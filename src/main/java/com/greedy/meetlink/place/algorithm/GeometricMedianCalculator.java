package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.domain.Coordinate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Weiszfeld 알고리즘으로 기하중심(Geometric Median) 계산
 *
 * 기하중심: 모든 참여자 좌표에 대해 거리 총합이 최소가 되는 점
 * 단순 무게중심(산술 평균)과 달리 이상치(outlier)에 강건함
 *
 * 알고리즘 참고:
 *   https://medium.com/@himanshu.sharma.for.work/optimal-geometric-location-using-the-weiszfeld-algorithm-d7fd6229da7c
 */
@Component
public class GeometricMedianCalculator {

    private static final int MAX_ITERATIONS = 300;
    private static final double CONVERGENCE_THRESHOLD = 1e-7; // 수렴 판정 임계값 (약 0.01m 수준)

    /**
     * 참여자 좌표 리스트로부터 기하중심 계산
     *
     * @param coordinates 참여자 출발지 좌표 목록 (2개 이상 권장)
     * @return 기하중심 좌표
     */
    public Coordinate calculate(List<Coordinate> coordinates) {
        if (coordinates.isEmpty()) {
            throw new IllegalArgumentException("좌표 목록이 비어 있습니다.");
        }
        if (coordinates.size() == 1) {
            return coordinates.get(0);
        }

        // 초기값: 단순 무게중심(산술 평균)으로 시작
        Coordinate current = arithmeticMean(coordinates);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            Coordinate next = weiszfeldStep(current, coordinates);

            // 이전 → 다음 이동 거리(km)가 임계값 이하면 수렴으로 판단
            if (current.distanceTo(next) < CONVERGENCE_THRESHOLD) {
                return next;
            }
            current = next;
        }

        return current; // 최대 반복 도달 시 마지막 값 반환
    }

    /**
     * Weiszfeld 1 스텝: 가중 평균으로 다음 좌표 계산
     *
     * next = Σ(xi / di) / Σ(1 / di)
     * di: 현재 추정점과 i번째 참여자 좌표 간 거리
     */
    private Coordinate weiszfeldStep(Coordinate current, List<Coordinate> coordinates) {
        double weightedLatSum = 0.0;
        double weightedLonSum = 0.0;
        double weightSum = 0.0;

        for (Coordinate coord : coordinates) {
            double distance = current.distanceTo(coord);

            // 현재 추정점과 좌표가 거의 일치하면 해당 좌표를 바로 반환
            // (0으로 나누기 방지)
            if (distance < 1e-10) {
                return coord;
            }

            double weight = 1.0 / distance;
            weightedLatSum += coord.latitude() * weight;
            weightedLonSum += coord.longitude() * weight;
            weightSum += weight;
        }

        return new Coordinate(weightedLatSum / weightSum, weightedLonSum / weightSum);
    }

    private Coordinate arithmeticMean(List<Coordinate> coordinates) {
        double avgLat = coordinates.stream()
                .mapToDouble(Coordinate::latitude)
                .average()
                .orElseThrow();
        double avgLon = coordinates.stream()
                .mapToDouble(Coordinate::longitude)
                .average()
                .orElseThrow();
        return new Coordinate(avgLat, avgLon);
    }
}
