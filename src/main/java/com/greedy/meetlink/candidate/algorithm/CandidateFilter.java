package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.client.TransitClient;
import com.greedy.meetlink.common.Coordinate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 후보 좌표 필터링 (1차: 거리 기반 / 2차: 이동시간 기반) */
@Component
@RequiredArgsConstructor
public class CandidateFilter {
    private static final double DISTANCE_THRESHOLD_FACTOR = 1.2;

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

    /** 2차 필터링: 이동 시간 기반 (MOTIS API 호출) — 이동시간 계산 실패 시 해당 후보 제외 */
    public List<FilteredCandidate> filterByTravelTime(
            List<Coordinate> candidates, List<Coordinate> participants) {

        List<FilteredCandidate> result = new ArrayList<>();

        for (Coordinate candidate : candidates) {
            List<ParticipantTravelTime> travelTimes = collectTravelTimes(candidate, participants);
            if (travelTimes != null) {
                result.add(new FilteredCandidate(candidate, travelTimes));
            }
        }

        return result;
    }

    private List<ParticipantTravelTime> collectTravelTimes(
            Coordinate candidate, List<Coordinate> participants) {
        List<ParticipantTravelTime> times = new ArrayList<>();
        for (Coordinate p : participants) {
            Double travelTime = transitClient.getTravelTimeSeconds(p, candidate);
            if (travelTime == null) return null;
            times.add(new ParticipantTravelTime(p, travelTime));
        }
        return times;
    }

    public record FilteredCandidate(
            Coordinate coordinate, List<ParticipantTravelTime> participantTravelTimes) {}

    public record ParticipantTravelTime(
            Coordinate participantCoordinate, double travelTimeSeconds) {}
}
