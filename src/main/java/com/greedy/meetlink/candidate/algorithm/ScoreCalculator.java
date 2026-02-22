package com.greedy.meetlink.candidate.algorithm;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 이동 시간 기반 점수 계산기 (공통 로직)
 *
 * <p>score = W_AVG × avg + W_MAX × max + W_STDDEV × stddev
 *
 * <p>점수가 낮을수록 좋음 (이동시간 최소화 + 공정성 최대화 목표)
 *
 * <p>[가중치 설계 근거] - avg(0.3): 전체 이동시간 평균. 모두가 가까울수록 유리. - max(0.3): 가장 멀리서 오는 참여자 보호. - stddev(0.4):
 * 공정성 핵심 지표. 편차가 작을수록 한 명에게 치우치지 않음.
 *
 * <p>[조정 방법] 비즈니스 요구사항에 따라 상수값 변경으로 손쉽게 튜닝 가능: - "최대한 오래 걸리는 사람 없게": W_MAX 올리기 - "평균적으로 가까운 장소 우선":
 * W_AVG 올리기 - "최대한 공평하게": W_STDDEV 올리기
 */
@Component
public class ScoreCalculator {

    private static final double W_AVG = 0.3;
    private static final double W_MAX = 0.3;
    private static final double W_STDDEV = 0.4;

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
