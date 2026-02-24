package com.greedy.meetlink.candidate.algorithm;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 이동 시간 기반 점수 계산기
 *
 * <p>score = W_AVG × avg + W_MAX × max + W_STDDEV × stddev (낮을수록 좋음)
 */
@Component
public class ScoreCalculator {

    private static final double W_AVG = 0.4;
    private static final double W_MAX = 0.4;
    private static final double W_STDDEV = 0.2;

    public ScoreResult calculate(List<Double> travelTimes) {
        if (travelTimes.isEmpty()) {
            return new ScoreResult(0.0, 0.0, 0.0, Double.MAX_VALUE);
        }

        double avg = travelTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double max = travelTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double stddev = calculateStddev(travelTimes, avg);
        double score = W_AVG * avg + W_MAX * max + W_STDDEV * stddev;

        return new ScoreResult(avg, max, stddev, score);
    }

    private double calculateStddev(List<Double> values, double avg) {
        if (values.size() <= 1) return 0.0;
        double variance =
                values.stream().mapToDouble(v -> Math.pow(v - avg, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }

    public record ScoreResult(double avg, double max, double stddev, double score) {}
}
