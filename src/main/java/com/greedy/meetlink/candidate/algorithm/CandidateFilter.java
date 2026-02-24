package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.common.Coordinate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** 후보 좌표 1차 필터링: 직선 거리 기반 */
@Component
public class CandidateFilter {
    private static final double DISTANCE_THRESHOLD_FACTOR = 1.2;

    /** 특정 참여자와의 직선 거리 > dMax × 1.2 이면 제거 */
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

    public record FilteredCandidate(
            Coordinate coordinate, List<ParticipantTravelTime> participantTravelTimes) {}

    public record ParticipantTravelTime(
            Coordinate participantCoordinate, double travelTimeSeconds) {}
}
