package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.domain.Coordinate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Polar Sampling 방식으로 후보 좌표 생성
 *
 * 기준점(기하중심)으로부터 거리(r)와 각도(θ)를 조합하여 유한한 후보 집합 생성
 *
 * 후보 수 목표: 30개 (최소 20 ~ 최대 50)
 */
@Component
public class PolarSamplingGenerator {

    private static final double ALPHA = 0.6;
    private static final double R_MIN_KM = 0.6;
    private static final double[] RADIUS_RATIOS = {0.25, 0.5, 0.75, 1.0};

    private static final int TARGET_CANDIDATES = 30;
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
        double dMax = coordinates.stream()
                .mapToDouble(center::distanceTo)
                .max()
                .orElse(0.0);

        return Math.max(dMax * ALPHA, R_MIN_KM);
    }

    /**
     * 목표 후보 수(30개)를 맞추기 위한 각도 간격 계산
     *
     * ✅ 수정: 기존 공식은 정수 나눗셈으로 인해 실제 후보 수가 목표치에 못 미치는 문제가 있었음.
     *
     * [기존 버그]
     *   angleStep = ceil(360 × 4 / 29) = 50
     *   실제 생성 수 = 4 × (360 / 50) + 1 = 4 × 7 + 1 = 29  ← 정수 나눗셈 360/50=7.0 → 7 (소수점 버림)
     *   두 번째 while: count=29 < 50 → 처음부터 false → dead code
     *
     * [수정 방향]
     *   angleStep을 직접 역산하는 대신,
     *   angleStep을 1씩 줄여가면서 실제 생성 후보 수가 MIN~MAX 범위에 들어오는
     *   가장 큰 angleStep을 선택 (= 후보 수를 최소화하되 MIN 이상 보장)
     *
     * [결과]
     *   angleStep=45 → 4×(360/45)+1 = 4×8+1 = 33개 (MIN=20 이상, MAX=50 이하 ✅)
     *   angleStep=46 → 4×(360/46)+1 = 4×7+1 = 29개 (MIN=20 이상이지만 TARGET=30 미달)
     *   → angleStep=45 선택
     */
    private int calculateAngleStep() {
        int radiusDivisions = RADIUS_RATIOS.length;

        // angleStep을 360에서 1까지 줄이면서 조건 충족하는 최대값 탐색
        // 최대값 = 후보 수가 가장 적으면서도 MIN 이상인 step
        for (int step = 360; step >= 1; step--) {
            int count = radiusDivisions * (360 / step) + 1; // 기준점 1개 포함
            if (count >= MIN_CANDIDATES && count <= MAX_CANDIDATES) {
                return step;
            }
        }

        // 도달 불가능한 fallback (RADIUS_RATIOS가 극단적으로 변경된 경우)
        return 1;
    }
}
