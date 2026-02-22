package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.candidate.algorithm.CandidateFilter.FilteredCandidate;
import com.greedy.meetlink.candidate.algorithm.CandidateFilter.ParticipantTravelTime;
import com.greedy.meetlink.candidate.algorithm.ScoreCalculator.ScoreResult;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 후보 좌표 점수 산정
 *
 * <p>score(P) = W_AVG × T_avg + W_MAX × T_max + W_STDDEV × T_stddev (가중치는 ScoreCalculator 참조) 점수가
 * 낮을수록 좋음 (이동시간 최소화 목표)
 */
@Component
@RequiredArgsConstructor
public class CandidateScorer {

    private final ScoreCalculator scoreCalculator;

    /**
     * 후보 목록을 점수 기준으로 정렬하여 반환 점수 낮은 순(이동시간 합이 최소) → rank 1이 가장 좋음
     *
     * @param candidates 2차 필터 통과 후보 목록
     * @param topK 반환할 상위 후보 수
     * @return 점수 + 순위가 계산된 후보 목록
     */
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
                        .map(ParticipantTravelTime::travelTimeMinutes)
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

    /** 점수가 산정된 후보 */
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
