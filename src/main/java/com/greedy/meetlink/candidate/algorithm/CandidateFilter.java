package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.client.TransitClient;
import com.greedy.meetlink.common.Coordinate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 후보 좌표 필터링 (1차: 거리 기반 / 2차: 이동시간 기반) */
@Component
@RequiredArgsConstructor
public class CandidateFilter {
    private static final double DISTANCE_THRESHOLD_FACTOR = 1.2;
    private static final double MAX_TRAVEL_TIME_SECONDS = 3600.0; // 60분
    private static final int SAMPLE_PARTICIPANT_COUNT = 3;

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
     * 2차 필터링: 이동 시간 기반 (MOTIS API 호출)
     *
     * <p>API 호출 최소화를 위해 기하중심에서 가장 먼 참여자(샘플)를 먼저 검증하고, 통과 시 나머지 참여자를 검증한다.
     */
    public List<FilteredCandidate> filterByTravelTime(
            List<Coordinate> candidates, List<Coordinate> participants, Coordinate center) {

        Set<Coordinate> sampleSet = selectSampleParticipants(participants, center);
        List<FilteredCandidate> result = new ArrayList<>();

        for (Coordinate candidate : candidates) {
            Map<Coordinate, Double> travelTimeCache = new HashMap<>();

            if (isFailedOnSample(candidate, sampleSet, travelTimeCache)) {
                continue;
            }

            if (isFailedOnRemainder(candidate, participants, sampleSet, travelTimeCache)) {
                continue;
            }

            List<ParticipantTravelTime> travelTimes =
                    participants.stream()
                            .map(p -> new ParticipantTravelTime(p, travelTimeCache.get(p)))
                            .collect(Collectors.toList());

            result.add(new FilteredCandidate(candidate, travelTimes));
        }

        return result;
    }

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
            Coordinate candidate, Set<Coordinate> sampleSet, Map<Coordinate, Double> cache) {

        for (Coordinate p : sampleSet) {
            Double travelTime = transitClient.getTravelTimeSeconds(p, candidate);
            if (isInvalidTravelTime(travelTime)) return true;
            cache.put(p, travelTime);
        }
        return false;
    }

    private boolean isFailedOnRemainder(
            Coordinate candidate,
            List<Coordinate> participants,
            Set<Coordinate> sampleSet,
            Map<Coordinate, Double> cache) {

        for (Coordinate p : participants) {
            if (sampleSet.contains(p)) continue;

            Double travelTime = transitClient.getTravelTimeSeconds(p, candidate);
            if (isInvalidTravelTime(travelTime)) return true;
            cache.put(p, travelTime);
        }
        return false;
    }

    /** 이동시간이 유효하지 않은 경우: null이거나 3600초(60분) 초과 */
    private boolean isInvalidTravelTime(Double travelTime) {
        return travelTime == null || travelTime > MAX_TRAVEL_TIME_SECONDS;
    }

    public record FilteredCandidate(
            Coordinate coordinate, List<ParticipantTravelTime> participantTravelTimes) {}

    public record ParticipantTravelTime(
            Coordinate participantCoordinate, double travelTimeSeconds) {}
}
