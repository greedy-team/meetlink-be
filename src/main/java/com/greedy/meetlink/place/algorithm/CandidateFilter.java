package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.client.TransitClient;
import com.greedy.meetlink.place.domain.Coordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 후보 좌표 필터링 (1차: 거리 기반 / 2차: 이동시간 기반)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CandidateFilter {

    private static final double DISTANCE_THRESHOLD_FACTOR = 1.2;
    private static final double MAX_TRAVEL_TIME_MINUTES = 60.0;
    private static final int SAMPLE_PARTICIPANT_COUNT = 3;

    /**
     * TMap 무료 플랜 초당 요청 제한 대응 딜레이 (ms)
     * 유료 플랜 전환 시 0으로 조정 가능
     * (FakeTransitClient 사용 시에는 딜레이가 사실상 불필요하지만, 로직 통일성을 위해 유지해도 무방)
     */
    private static final long TMAP_CALL_DELAY_MS = 1000L;

    // [수정] 구체 클래스 대신 인터페이스 사용 (Real/Fake 교체 가능)
    private final TransitClient transitClient;

    /**
     * 1차 필터링: 직선 거리 기반
     * 특정 참여자와의 직선 거리 > D_max × 1.2 이면 제거
     */
    public List<Coordinate> filterByDistance(
            List<Coordinate> candidates,
            List<Coordinate> participants,
            double dMax) {

        double threshold = dMax * DISTANCE_THRESHOLD_FACTOR;

        return candidates.stream()
                .filter(candidate -> participants.stream()
                        .allMatch(p -> candidate.distanceTo(p) <= threshold))
                .collect(Collectors.toList());
    }

    /**
     * 2차 필터링: 이동 시간 기반 (TMap API 호출)
     */
    public List<FilteredCandidate> filterByTravelTime(
            List<Coordinate> candidates,
            List<Coordinate> participants,
            Coordinate center) {

        Set<Coordinate> sampleSet = participants.stream()
                .sorted(Comparator.comparingDouble(center::distanceTo).reversed())
                .limit(Math.min(SAMPLE_PARTICIPANT_COUNT, participants.size()))
                .collect(Collectors.toSet());

        List<FilteredCandidate> result = new ArrayList<>();

        for (Coordinate candidate : candidates) {

            Map<Coordinate, Double> travelTimeCache = new HashMap<>();
            boolean failed = false;

            // 단계 1: 샘플 참여자 이동시간 검증
            for (Coordinate p : participants) {
                if (!sampleSet.contains(p)) continue;

                Double travelTime = callWithDelay(p, candidate);
                if (travelTime == null || travelTime > MAX_TRAVEL_TIME_MINUTES) {
                    log.debug("샘플 필터 탈락: candidate={}, participant={}, time={}",
                            candidate, p, travelTime);
                    failed = true;
                    break;
                }
                travelTimeCache.put(p, travelTime);
            }

            if (failed) continue;

            // 단계 2: 나머지 참여자 이동시간 계산
            for (Coordinate p : participants) {
                if (sampleSet.contains(p)) continue;

                Double travelTime = callWithDelay(p, candidate);
                if (travelTime == null) {
                    log.warn("이동시간 조회 실패: participant={}, candidate={}", p, candidate);
                    failed = true;
                    break;
                }
                travelTimeCache.put(p, travelTime);
            }

            if (failed) continue;

            // 순서 보장 조립
            List<ParticipantTravelTime> travelTimes = participants.stream()
                    .map(p -> new ParticipantTravelTime(p, travelTimeCache.get(p)))
                    .collect(Collectors.toList());

            result.add(new FilteredCandidate(candidate, travelTimes));
        }

        return result;
    }

    private Double callWithDelay(Coordinate origin, Coordinate destination) {
        // [수정] 인터페이스 메서드 호출
        Double result = transitClient.getTravelTimeMinutes(origin, destination);
        try {
            Thread.sleep(TMAP_CALL_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("TMap API 딜레이 중 인터럽트. 해당 후보 처리를 중단합니다.");
            return null;
        }
        return result;
    }

    public record FilteredCandidate(
            Coordinate coordinate,
            List<ParticipantTravelTime> participantTravelTimes
    ) {}

    public record ParticipantTravelTime(
            Coordinate participantCoordinate,
            double travelTimeMinutes
    ) {}
}
