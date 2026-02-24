package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.candidate.algorithm.CandidateFilter.FilteredCandidate;
import com.greedy.meetlink.candidate.algorithm.CandidateFilter.ParticipantTravelTime;
import com.greedy.meetlink.candidate.algorithm.ScoreCalculator.ScoreResult;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 후보 좌표 점수 산정 및 상위 K개 선발 */
@Component
@RequiredArgsConstructor
public class CandidateScorer {

    private final ScoreCalculator scoreCalculator;

    public List<FilteredCandidate> score(List<FilteredCandidate> candidates, int topK) {
        return candidates.stream()
                .sorted(Comparator.comparingDouble(this::calculateScore))
                .limit(topK)
                .collect(Collectors.toList());
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
