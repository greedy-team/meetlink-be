package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.place.Coordinate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** 기하중심으로부터 거리(r)와 각도(θ)를 조합한 Polar Sampling으로 후보 좌표 생성 */
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
