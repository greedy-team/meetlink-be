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

    public List<ScoredCandidate> score(List<FilteredCandidate> candidates, int topK) {
        List<ScoredCandidate> scored =
                candidates.stream()
                        .map(this::calculateScore)
                        .sorted(Comparator.comparingDouble(ScoredCandidate::score))
                        .limit(topK)
                        .collect(Collectors.toList());

        for (int i = 0; i < scored.size(); i++) {
            scored.set(i, scored.get(i).withRank(i + 1));
        }

        return scored;
    }

    private ScoredCandidate calculateScore(FilteredCandidate candidate) {
        List<Double> times =
                candidate.participantTravelTimes().stream()
                        .map(ParticipantTravelTime::travelTimeSeconds)
                        .collect(Collectors.toList());

        ScoreResult result = scoreCalculator.calculate(times);

        return new ScoredCandidate(
                candidate,
                result.avg(),
                result.max(),
                result.stddev(),
                result.score(),
                0 // rank는 score() 호출 후 withRank()로 부여
                );
    }

    public record ScoredCandidate(
            FilteredCandidate filteredCandidate,
            double avgTravelTime,
            double maxTravelTime,
            double stddevTravelTime,
            double score,
            int rank) {
        public ScoredCandidate withRank(int rank) {
            return new ScoredCandidate(
                    filteredCandidate, avgTravelTime, maxTravelTime, stddevTravelTime, score, rank);
        }
    }
}
