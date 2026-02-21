package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.domain.Coordinate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Weiszfeld 알고리즘으로 기하중심(Geometric Median) 계산
 *
 * 기하중심: 모든 참여자 좌표에 대해 거리 총합이 최소가 되는 점
 * 단순 무게중심(산술 평균)과 달리 이상치(outlier)에 강건함
 */
@Component
public class GeometricMedianCalculator {

    private static final int MAX_ITERATIONS = 300;

    /**
     * 수렴 판정 임계값 (km)
     *
     * ✅ 수정: 기존 1e-7은 주석에 "약 0.01m 수준"이라 적혀 있었으나,
     *    distanceTo()가 km 단위를 반환하므로 실제로는 0.0001mm (= 0.1 마이크로미터)로
     *    사실상 수렴 불가 수준이었음. 항상 MAX_ITERATIONS(300회)를 모두 돔.
     *
     *    1e-4 km = 0.1m 수준으로 조정.
     *    서울 시내 기준 충분한 정밀도이며, 보통 10~30회 이내에 수렴함.
     */
    private static final double CONVERGENCE_THRESHOLD_KM = 1e-4;

    /**
     * 참여자 좌표 리스트로부터 기하중심 계산
     */
    public Coordinate calculate(List<Coordinate> coordinates) {
        if (coordinates.isEmpty()) {
            throw new IllegalArgumentException("좌표 목록이 비어 있습니다.");
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

        return current;
    }

    /**
     * Weiszfeld 1 스텝: 거리의 역수를 가중치로 한 가중 평균
     *
     * next = Σ(xi / di) / Σ(1 / di)
     * di: 현재 추정점과 i번째 참여자 좌표 간 거리(km)
     */
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
        double avgLat = coordinates.stream().mapToDouble(Coordinate::latitude).average().orElseThrow();
        double avgLon = coordinates.stream().mapToDouble(Coordinate::longitude).average().orElseThrow();
        return new Coordinate(avgLat, avgLon);
    }
}
