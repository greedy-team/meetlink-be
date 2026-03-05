package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.candidate.algorithm.CandidateFilter.FilteredCandidate;
import com.greedy.meetlink.candidate.algorithm.CandidateFilter.ParticipantTravelTime;
import com.greedy.meetlink.candidate.algorithm.ScoreCalculator.ScoreResult;
import com.greedy.meetlink.common.client.TransitClient;
import com.greedy.meetlink.common.client.dto.RouteInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 후보 좌표 이동시간 수집 및 점수 기반 상위 K개 선발 */
@Component
@RequiredArgsConstructor
public class CandidateScorer {

    private final TransitClient transitClient;
    private final ScoreCalculator scoreCalculator;

    public List<FilteredCandidate> selectTop(
            List<Coordinate> candidates, List<Coordinate> participants, int topK) {
        return candidates.stream()
                .map(candidate -> fetchTravelTimes(candidate, participants))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(this::calculateScore))
                .limit(topK)
                .collect(Collectors.toList());
    }

    private FilteredCandidate fetchTravelTimes(
            Coordinate candidate, List<Coordinate> participants) {
        List<ParticipantTravelTime> times = new ArrayList<>();
        for (Coordinate p : participants) {
            RouteInfo plan =
                    transitClient.getPlan(
                            p.latitude(),
                            p.longitude(),
                            candidate.latitude(),
                            candidate.longitude());
            if (plan == null) return null;
            times.add(new ParticipantTravelTime(p, plan.travelTime()));
        }
        return new FilteredCandidate(candidate, times);
    }

    private double calculateScore(FilteredCandidate candidate) {
        List<Double> times =
                candidate.participantTravelTimes().stream()
                        .map(ParticipantTravelTime::travelTimeSeconds)
                        .collect(Collectors.toList());

        ScoreResult result = scoreCalculator.calculate(times);
        return result.score();
    }
}
