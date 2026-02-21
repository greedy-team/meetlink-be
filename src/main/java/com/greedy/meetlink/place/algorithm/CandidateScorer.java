package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.algorithm.CandidateFilter.FilteredCandidate;
import com.greedy.meetlink.place.algorithm.CandidateFilter.ParticipantTravelTime;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 후보 좌표 점수 산정
 *
 * score(P) = w1 × T_avg + w2 × T_max + w3 × T_stddev
 *   w1 = 0.4  (평균 이동시간)
 *   w2 = 0.4  (최대 이동시간)
 *   w3 = 0.2  (이동시간 표준편차)
 *
 * 점수가 낮을수록 좋음 (이동시간 최소화 목표)
 */
@Component
public class CandidateScorer {

    private static final double W1_AVG = 0.4;
    private static final double W2_MAX = 0.4;
    private static final double W3_STDDEV = 0.2;

    /**
     * 후보 목록을 점수 기준으로 정렬하여 반환
     * 점수 낮은 순(이동시간 합이 최소) → rank 1이 가장 좋음
     *
     * @param candidates 2차 필터 통과 후보 목록
     * @param topK       반환할 상위 후보 수
     * @return 점수 + 순위가 계산된 후보 목록
     */
    public List<ScoredCandidate> score(List<FilteredCandidate> candidates, int topK) {
        List<ScoredCandidate> scored = candidates.stream()
                .map(this::calculateScore)
                .sorted(Comparator.comparingDouble(ScoredCandidate::score))
                .limit(topK)
                .collect(Collectors.toList());

        // 순위 부여 (1부터 시작)
        for (int i = 0; i < scored.size(); i++) {
            scored.set(i, scored.get(i).withRank(i + 1));
        }

        return scored;
    }

    private ScoredCandidate calculateScore(FilteredCandidate candidate) {
        List<Double> times = candidate.participantTravelTimes().stream()
                .map(ParticipantTravelTime::travelTimeMinutes)
                .collect(Collectors.toList());

        double avg = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double max = times.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double stddev = calculateStddev(times, avg);

        double score = W1_AVG * avg + W2_MAX * max + W3_STDDEV * stddev;

        return new ScoredCandidate(candidate, avg, max, stddev, score, 0);
    }

    private double calculateStddev(List<Double> values, double avg) {
        if (values.size() <= 1) return 0.0;
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - avg, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    /**
     * 점수가 산정된 후보
     */
    public record ScoredCandidate(
            FilteredCandidate filteredCandidate,
            double avgTravelTime,
            double maxTravelTime,
            double stddevTravelTime,
            double score,
            int rank
    ) {
        public ScoredCandidate withRank(int rank) {
            return new ScoredCandidate(
                    filteredCandidate, avgTravelTime, maxTravelTime, stddevTravelTime, score, rank);
        }
    }
}
