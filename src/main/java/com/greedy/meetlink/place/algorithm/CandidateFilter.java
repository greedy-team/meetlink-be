package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.client.TMapTransitClient;
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
     */
    private static final long TMAP_CALL_DELAY_MS = 1000L;

    private final TMapTransitClient tMapTransitClient;

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
     *
     * 단계 1 - 가장 멀리 떨어진 참여자 최대 3명을 샘플로 먼저 검증
     *   → 샘플 이동시간이 60분 초과면 탈락
     * 단계 2 - 샘플 통과 후보에 대해서만 나머지 참여자 이동시간 계산
     *
     * ✅ 순서 보장:
     *   반환되는 travelTimes는 항상 participants 입력 순서와 동일한 인덱스를 유지합니다.
     *   중간에 샘플/나머지로 분리해서 API를 호출하더라도,
     *   캐시(Map)에 저장한 뒤 최종 조립 시 participants 순서를 기준으로 재구성합니다.
     *   → saveFinalCandidates의 locations.get(i) ↔ travelTimes.get(i) 인덱스 매핑이 정확합니다.
     */
    public List<FilteredCandidate> filterByTravelTime(
            List<Coordinate> candidates,
            List<Coordinate> participants,
            Coordinate center) {

        // 샘플 참여자 선정: 기준점에서 가장 먼 순으로 최대 3명
        // Set으로 보관하여 2단계에서 O(1) 조회
        Set<Coordinate> sampleSet = participants.stream()
                .sorted(Comparator.comparingDouble(center::distanceTo).reversed())
                .limit(Math.min(SAMPLE_PARTICIPANT_COUNT, participants.size()))
                .collect(Collectors.toSet());

        List<FilteredCandidate> result = new ArrayList<>();

        for (Coordinate candidate : candidates) {

            // 이동시간 캐시: 샘플/나머지 구분 없이 좌표 → 이동시간으로 저장
            Map<Coordinate, Double> travelTimeCache = new HashMap<>();
            boolean failed = false;

            // 단계 1: 샘플 참여자 이동시간 검증
            // participants 순서를 유지하면서 샘플만 처리
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

            // ✅ 핵심 수정: participants 원래 순서대로 결과 조립
            // 샘플을 먼저 처리했더라도 최종 리스트는 항상 participants 입력 순서 기준
            List<ParticipantTravelTime> travelTimes = participants.stream()
                    .map(p -> new ParticipantTravelTime(p, travelTimeCache.get(p)))
                    .collect(Collectors.toList());

            result.add(new FilteredCandidate(candidate, travelTimes));
        }

        return result;
    }

    /**
     * TMap API 호출 + 쿼터 보호 딜레이
     *
     * ✅ 수정: InterruptedException catch 후 Thread.currentThread().interrupt()로
     *    플래그를 복구하고 null을 반환하여 상위 로직이 정상 종료되도록 처리
     *    (기존 코드는 플래그 소실로 인해 외부 취소 신호가 무시됨)
     */
    private Double callWithDelay(Coordinate origin, Coordinate destination) {
        Double result = tMapTransitClient.getTravelTimeMinutes(origin, destination);
        try {
            Thread.sleep(TMAP_CALL_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // ✅ 인터럽트 플래그 복구
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
