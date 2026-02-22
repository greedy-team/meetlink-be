package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.place.Coordinate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Polar Sampling 방식으로 후보 좌표 생성
 *
 * <p>기준점(기하중심)으로부터 거리(r)와 각도(θ)를 조합하여 유한한 후보 집합 생성
 *
 * <p>후보 수 목표: 30개 (최소 20 ~ 최대 50)
 */
@Component
public class PolarSamplingGenerator {

    private static final double ALPHA = 0.6;
    private static final double R_MIN_KM = 0.6;
    private static final double[] RADIUS_RATIOS = {0.25, 0.5, 0.75, 1.0};

    private static final int MIN_CANDIDATES = 20;
    private static final int MAX_CANDIDATES = 50;

    public List<Coordinate> generate(Coordinate center, List<Coordinate> coordinates) {
        double radius = calculateRadius(center, coordinates);
        int angleStep = calculateAngleStep();

        List<Coordinate> candidates = new ArrayList<>();
        candidates.add(center);

        for (double ratio : RADIUS_RATIOS) {
            double r = radius * ratio;
            for (int angle = 0; angle < 360; angle += angleStep) {
                candidates.add(center.move(r, angle));
                if (candidates.size() >= MAX_CANDIDATES) {
                    return candidates;
                }
            }
        }

        return candidates;
    }

    public double calculateRadius(Coordinate center, List<Coordinate> coordinates) {
        double dMax = coordinates.stream().mapToDouble(center::distanceTo).max().orElse(0.0);

        return Math.max(dMax * ALPHA, R_MIN_KM);
    }

    /**
     * 목표 후보 수(30개)를 맞추기 위한 각도 간격 계산
     *
     * <p>✅ 수정: 기존 공식은 정수 나눗셈으로 인해 실제 후보 수가 목표치에 못 미치는 문제가 있었음.
     *
     * <p>[기존 버그] angleStep = ceil(360 × 4 / 29) = 50 실제 생성 수 = 4 × (360 / 50) + 1 = 4 × 7 + 1 = 29
     * ← 정수 나눗셈 360/50=7.0 → 7 (소수점 버림)
     *
     * <p>[수정 방향] angleStep을 1씩 줄여가면서 실제 생성 후보 수가 MIN~MAX 범위에 들어오는 가장 큰 angleStep을 선택
     *
     * <p>[결과] angleStep=45 → 4×(360/45)+1 = 4×8+1 = 33개 (MIN=20 이상, MAX=50 이하 ✅)
     */
    private int calculateAngleStep() {
        int radiusDivisions = RADIUS_RATIOS.length;

        for (int step = 360; step >= 1; step--) {
            int count = radiusDivisions * (360 / step) + 1;
            if (count >= MIN_CANDIDATES && count <= MAX_CANDIDATES) {
                return step;
            }
        }

        return 1;
    }
}
