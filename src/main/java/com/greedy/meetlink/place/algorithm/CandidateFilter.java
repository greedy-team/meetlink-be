package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.client.TransitClient;
import com.greedy.meetlink.place.domain.Coordinate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 후보 좌표 필터링 (1차: 거리 기반 / 2차: 이동시간 기반) */
@Slf4j
@Component
@RequiredArgsConstructor
public class CandidateFilter {

    private static final double DISTANCE_THRESHOLD_FACTOR = 1.2;
    private static final double MAX_TRAVEL_TIME_MINUTES = 60.0;
    private static final int SAMPLE_PARTICIPANT_COUNT = 3;

    /**
     * ✅ 리팩토링: 하드코딩 1000ms → application.yml 외부화
     *
     * <p>application.yml 예시: tmap: call-delay-ms: 1000 # 무료 플랜 # call-delay-ms: 0 # 유료 플랜
     *
     * <p>FakeTransitClient 사용 시 0으로 설정하면 테스트 속도 향상.
     */
    @Value("${tmap.call-delay-ms:1000}")
    private long tmapCallDelayMs;

    private final TransitClient transitClient;

    /** 1차 필터링: 직선 거리 기반 특정 참여자와의 직선 거리 > dMax × 1.2 이면 제거 */
    public List<Coordinate> filterByDistance(
            List<Coordinate> candidates, List<Coordinate> participants, double dMax) {

        double threshold = dMax * DISTANCE_THRESHOLD_FACTOR;

        return candidates.stream()
                .filter(
                        candidate ->
                                participants.stream()
                                        .allMatch(p -> candidate.distanceTo(p) <= threshold))
                .collect(Collectors.toList());
    }

    /**
     * 2차 필터링: 이동 시간 기반 (TMap API 호출)
     *
     * <p>[전략] API 호출 최소화를 위해 2단계로 진행 1단계: 기하중심에서 가장 먼 참여자(샘플) 먼저 검증 → 60분 초과 시 즉시 탈락 2단계: 나머지 참여자
     * 검증 → API 실패 or 60분 초과 시 탈락
     *
     * <p>✅ 리팩토링: 기존 2단계 루프에서 나머지 참여자는 null 체크만 했으나, MAX_TRAVEL_TIME_MINUTES 초과도 탈락 조건에 포함되도록 수정.
     * (샘플만 시간 제한을 받는 비대칭 로직 버그 수정)
     */
    public List<FilteredCandidate> filterByTravelTime(
            List<Coordinate> candidates, List<Coordinate> participants, Coordinate center) {

        Set<Coordinate> sampleSet = selectSampleParticipants(participants, center);
        List<FilteredCandidate> result = new ArrayList<>();

        for (Coordinate candidate : candidates) {
            Map<Coordinate, Double> travelTimeCache = new HashMap<>();

            // 1단계: 샘플 참여자 이동시간 검증 (조기 탈락 최적화)
            if (isFailedOnSample(candidate, participants, sampleSet, travelTimeCache)) {
                continue;
            }

            // 2단계: 나머지 참여자 이동시간 검증
            if (isFailedOnRemainder(candidate, participants, sampleSet, travelTimeCache)) {
                continue;
            }

            // 참여자 순서를 보장하여 조립
            List<ParticipantTravelTime> travelTimes =
                    participants.stream()
                            .map(p -> new ParticipantTravelTime(p, travelTimeCache.get(p)))
                            .collect(Collectors.toList());

            result.add(new FilteredCandidate(candidate, travelTimes));
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // private helpers
    // -------------------------------------------------------------------------

    /** 기하중심에서 가장 먼 순으로 샘플 참여자 선택 */
    private Set<Coordinate> selectSampleParticipants(
            List<Coordinate> participants, Coordinate center) {
        return participants.stream()
                .sorted(Comparator.comparingDouble(center::distanceTo).reversed())
                .limit(Math.min(SAMPLE_PARTICIPANT_COUNT, participants.size()))
                .collect(Collectors.toSet());
    }

    /** 샘플 참여자 검증 — 실패 시 true 반환 */
    private boolean isFailedOnSample(
            Coordinate candidate,
            List<Coordinate> participants,
            Set<Coordinate> sampleSet,
            Map<Coordinate, Double> cache) {

        for (Coordinate p : participants) {
            if (!sampleSet.contains(p)) continue;

            Double travelTime = callWithDelay(p, candidate);
            if (isInvalidTravelTime(travelTime)) {
                log.debug(
                        "샘플 필터 탈락: candidate={}, participant={}, time={}",
                        candidate,
                        p,
                        travelTime);
                return true;
            }
            cache.put(p, travelTime);
        }
        return false;
    }

    /**
     * 나머지 참여자 검증 — 실패 시 true 반환
     *
     * <p>✅ 리팩토링: null 체크 외에 MAX_TRAVEL_TIME_MINUTES 초과도 탈락 조건 추가
     */
    private boolean isFailedOnRemainder(
            Coordinate candidate,
            List<Coordinate> participants,
            Set<Coordinate> sampleSet,
            Map<Coordinate, Double> cache) {

        for (Coordinate p : participants) {
            if (sampleSet.contains(p)) continue;

            Double travelTime = callWithDelay(p, candidate);
            if (isInvalidTravelTime(travelTime)) {
                log.warn(
                        "나머지 참여자 필터 탈락: candidate={}, participant={}, time={}",
                        candidate,
                        p,
                        travelTime);
                return true;
            }
            cache.put(p, travelTime);
        }
        return false;
    }

    /** 이동시간이 유효하지 않은 경우: null이거나 60분 초과 */
    private boolean isInvalidTravelTime(Double travelTime) {
        return travelTime == null || travelTime > MAX_TRAVEL_TIME_MINUTES;
    }

    /** TMap API 호출 + 딜레이 ✅ 리팩토링: 딜레이값을 @Value로 주입받아 환경별 조정 가능 */
    private Double callWithDelay(Coordinate origin, Coordinate destination) {
        Double result = transitClient.getTravelTimeMinutes(origin, destination);
        if (tmapCallDelayMs > 0) {
            try {
                Thread.sleep(tmapCallDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("TMap API 딜레이 중 인터럽트. 해당 후보 처리를 중단합니다.");
                return null;
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // result types
    // -------------------------------------------------------------------------

    public record FilteredCandidate(
            Coordinate coordinate, List<ParticipantTravelTime> participantTravelTimes) {}

    public record ParticipantTravelTime(
            Coordinate participantCoordinate, double travelTimeMinutes) {}
}
