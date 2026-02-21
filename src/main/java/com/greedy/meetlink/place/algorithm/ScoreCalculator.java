package com.greedy.meetlink.place.algorithm;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 이동 시간 기반 점수 계산기 (공통 로직)
 */
@Component
public class ScoreCalculator {

    private static final double W1_AVG = 0.4;
    private static final double W2_MAX = 0.4;
    private static final double W3_STDDEV = 0.2;

    public ScoreResult calculate(List<Double> travelTimes) {
        if (travelTimes.isEmpty()) {
            return new ScoreResult(0.0, 0.0, 0.0, Double.MAX_VALUE);
        }

        double avg = travelTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double max = travelTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double stddev = calculateStddev(travelTimes, avg);

        double score = W1_AVG * avg + W2_MAX * max + W3_STDDEV * stddev;

        return new ScoreResult(avg, max, stddev, score);
    }

    private double calculateStddev(List<Double> values, double avg) {
        if (values.size() <= 1) return 0.0;
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - avg, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    public record ScoreResult(
        double avg,
        double max,
        double stddev,
        double score
    ) {}
}
