package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.client.TMapTransitClient;
import com.greedy.meetlink.place.domain.Coordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 후보 좌표 필터링 (1차: 거리 기반 / 2차: 이동시간 기반)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CandidateFilter {

    // 1차 필터: 허용 임계값 계수 = D_max × 1.2
    private static final double DISTANCE_THRESHOLD_FACTOR = 1.2;

    // 2차 필터: 샘플 평가 최대 이동 시간 (분)
    private static final double MAX_TRAVEL_TIME_MINUTES = 60.0;

    // 2차 샘플 평가: 가장 먼 참여자 2~3명만 먼저 검증
    private static final int SAMPLE_PARTICIPANT_COUNT = 3;

    private final TMapTransitClient tMapTransitClient;

    /**
     * 1차 필터링: 직선 거리 기반
     * 특정 참여자와의 직선 거리 > D_max × 1.2 이면 제거
     *
     * @param candidates   후보 좌표 목록
     * @param participants 참여자 출발지 좌표 목록
     * @param dMax         참여자 중 기준점으로부터 가장 먼 직선 거리(km)
     * @return 1차 필터 통과 후보 목록
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
     * 단계 1 - 가장 멀리 떨어진 참여자 2~3명을 샘플로 먼저 검증 (이동시간 캐싱)
     *   → 샘플 평가 최대 이동시간 > 60분이면 탈락
     * 단계 2 - 샘플 통과 후보에 대해서만 나머지 참여자 이동시간 계산
     *
     * @param candidates   1차 필터 통과 후보
     * @param participants 참여자 출발지 좌표 목록
     * @param center       기준점 (거리 기반 샘플 정렬 기준)
     * @return 이동 시간 포함 통과 후보 목록
     */
    public List<FilteredCandidate> filterByTravelTime(
            List<Coordinate> candidates,
            List<Coordinate> participants,
            Coordinate center) {

        // 기준점으로부터 거리 기준 정렬하여 샘플 참여자 추출
        List<Coordinate> sampleParticipants = participants.stream()
                .sorted(Comparator.comparingDouble(center::distanceTo).reversed())
                .limit(Math.min(SAMPLE_PARTICIPANT_COUNT, participants.size()))
                .collect(Collectors.toList());

        // 샘플 참여자를 Set으로 변환하여 빠른 조회 (나머지 참여자 식별용)
        Set<Coordinate> sampleSet = new HashSet<>(sampleParticipants);

        List<FilteredCandidate> result = new ArrayList<>();

        for (Coordinate candidate : candidates) {
            List<ParticipantTravelTime> travelTimes = new ArrayList<>();
            boolean passedSample = true;

            // 단계 1: 샘플 참여자 이동시간 검증 및 저장
            for (Coordinate sample : sampleParticipants) {
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                Double travelTime = tMapTransitClient.getTravelTimeMinutes(sample, candidate);
                
                if (travelTime == null || travelTime > MAX_TRAVEL_TIME_MINUTES) {
                    passedSample = false;
                    break;
                }
                travelTimes.add(new ParticipantTravelTime(sample, travelTime));
            }

            if (!passedSample) {
                log.debug("샘플 필터 탈락: {}", candidate);
                continue;
            }

            // 단계 2: 나머지 참여자 이동시간 계산
            boolean passedAll = true;
            for (Coordinate p : participants) {
                if (sampleSet.contains(p)) continue; // 이미 계산한 샘플은 스킵

                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                Double travelTime = tMapTransitClient.getTravelTimeMinutes(p, candidate);
                if (travelTime == null) {
                    log.warn("이동시간 조회 실패: participant={}, candidate={}", p, candidate);
                    passedAll = false;
                    break;
                }
                travelTimes.add(new ParticipantTravelTime(p, travelTime));
            }

            if (passedAll) {
                result.add(new FilteredCandidate(candidate, travelTimes));
            }
        }

        return result;
    }

    /**
     * 2차 필터를 통과한 후보 좌표 + 전체 참여자 이동시간 목록
     */
    public record FilteredCandidate(
            Coordinate coordinate,
            List<ParticipantTravelTime> participantTravelTimes
    ) {}

    /**
     * 참여자 좌표 + 해당 후보 지점까지의 이동 시간(분)
     */
    public record ParticipantTravelTime(
            Coordinate participantCoordinate,
            double travelTimeMinutes
    ) {}
}
