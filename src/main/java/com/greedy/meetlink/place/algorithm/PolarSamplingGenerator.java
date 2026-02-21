package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.domain.Coordinate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Polar Sampling 방식으로 후보 좌표 생성
 *
 * 연속 공간을 그대로 탐색하지 않고, 기준점(기하중심)으로부터
 * 거리(r)와 각도(θ)를 조합하여 유한한 후보 집합 생성
 *
 * 후보 수 목표: 30개 (최소 20 ~ 최대 50)
 */
@Component
public class PolarSamplingGenerator {

    // 탐색 반경 조절 인자 α (0.5 ~ 0.7)
    private static final double ALPHA = 0.6;

    // 최소 탐색 반경 600m
    private static final double R_MIN_KM = 0.6;

    // 반경 분할 비율: r ∈ {0.25R, 0.5R, 0.75R, R}
    private static final double[] RADIUS_RATIOS = {0.25, 0.5, 0.75, 1.0};

    private static final int TARGET_CANDIDATES = 30;
    private static final int MIN_CANDIDATES = 20;
    private static final int MAX_CANDIDATES = 50;

    /**
     * 후보 좌표 생성
     *
     * @param center       탐색 기준점 (기하중심)
     * @param coordinates  참여자 좌표 목록
     * @return             후보 좌표 목록 (기준점 포함)
     */
    public List<Coordinate> generate(Coordinate center, List<Coordinate> coordinates) {
        double radius = calculateRadius(center, coordinates);
        int angleStep = calculateAngleStep();

        List<Coordinate> candidates = new ArrayList<>();
        candidates.add(center); // 기하중심은 항상 후보에 포함

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

    /**
     * 탐색 반경 계산
     * R = max(D_max × α, R_min)
     *
     * @param center      기준점
     * @param coordinates 참여자 좌표
     * @return 탐색 반경 (km)
     */
    public double calculateRadius(Coordinate center, List<Coordinate> coordinates) {
        double dMax = coordinates.stream()
                .mapToDouble(center::distanceTo)
                .max()
                .orElse(0.0);

        return Math.max(dMax * ALPHA, R_MIN_KM);
    }

    /**
     * 목표 후보 수(30개)를 맞추기 위한 각도 간격 계산
     * 목표 개수 = 반경 분할 수 × (360 / angleStep)
     * → angleStep = 360 × 반경 분할 수 / (목표 개수 - 1)  [기준점 1개 제외]
     */
    private int calculateAngleStep() {
        int radiusDivisions = RADIUS_RATIOS.length;
        // 기준점 제외한 나머지 후보 수
        int sampleCount = TARGET_CANDIDATES - 1;
        int angleStep = (int) Math.ceil(360.0 * radiusDivisions / sampleCount);

        // 후보 수 범위 내로 조정 (MIN_CANDIDATES ~ MAX_CANDIDATES)
        while (radiusDivisions * (360 / angleStep) + 1 < MIN_CANDIDATES && angleStep > 1) {
            angleStep--;
        }
        while (radiusDivisions * (360 / angleStep) + 1 > MAX_CANDIDATES) {
            angleStep++;
        }

        return Math.max(angleStep, 1);
    }
}
