package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.common.Coordinate;
import com.greedy.meetlink.common.exception.EmptyCoordinatesException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Weiszfeld 알고리즘으로 기하중심(Geometric Median) 계산 */
@Slf4j
@Component
public class GeometricMedianCalculator {

    private static final int MAX_ITERATIONS = 300;

    private static final double CONVERGENCE_THRESHOLD_KM = 1e-4; // 0.1m

    public Coordinate calculate(List<Coordinate> coordinates) {
        if (coordinates.isEmpty()) {
            throw new EmptyCoordinatesException();
        }
        if (coordinates.size() == 1) {
            return coordinates.get(0);
        }

        Coordinate current = arithmeticMean(coordinates);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            Coordinate next = weiszfeldStep(current, coordinates);

            if (current.distanceTo(next) < CONVERGENCE_THRESHOLD_KM) {
                return next;
            }
            current = next;
        }

        log.warn("Geometric median 수렴 실패: {}회 반복 후 근사값 반환", MAX_ITERATIONS);
        return current;
    }

    // next = Σ(xi / di) / Σ(1 / di)
    private Coordinate weiszfeldStep(Coordinate current, List<Coordinate> coordinates) {
        double weightedLatSum = 0.0;
        double weightedLonSum = 0.0;
        double weightSum = 0.0;

        for (Coordinate coord : coordinates) {
            double distance = current.distanceTo(coord);

            if (distance < 1e-10) {
                return coord; // 0 나눗셈 방지: 현재 추정점이 좌표와 거의 일치
            }

            double weight = 1.0 / distance;
            weightedLatSum += coord.latitude() * weight;
            weightedLonSum += coord.longitude() * weight;
            weightSum += weight;
        }

        return new Coordinate(weightedLatSum / weightSum, weightedLonSum / weightSum);
    }

    private Coordinate arithmeticMean(List<Coordinate> coordinates) {
        double avgLat =
                coordinates.stream().mapToDouble(Coordinate::latitude).average().orElseThrow();
        double avgLon =
                coordinates.stream().mapToDouble(Coordinate::longitude).average().orElseThrow();
        return new Coordinate(avgLat, avgLon);
    }
}
