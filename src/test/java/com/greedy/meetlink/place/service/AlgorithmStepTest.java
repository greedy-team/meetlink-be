package com.greedy.meetlink.place.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.greedy.meetlink.candidate.algorithm.*;
import com.greedy.meetlink.candidate.algorithm.CandidateFilter.FilteredCandidate;
import com.greedy.meetlink.candidate.algorithm.CandidateScorer.ScoredCandidate;
import com.greedy.meetlink.place.Coordinate;
import com.greedy.meetlink.place.client.TransitClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlgorithmStepTest {

    @Mock private TransitClient transitClient;

    private GeometricMedianCalculator geometricMedianCalculator;
    private PolarSamplingGenerator polarSamplingGenerator;
    private CandidateFilter candidateFilter;
    private CandidateScorer candidateScorer;

    @BeforeEach
    void setUp() {
        geometricMedianCalculator = new GeometricMedianCalculator();
        polarSamplingGenerator = new PolarSamplingGenerator();
        candidateFilter = new CandidateFilter(transitClient);

        candidateScorer = new CandidateScorer(new ScoreCalculator());
    }

    @Test
    @DisplayName("Step 1: 기하중심(Geometric Median)은 참여자들 사이에 위치해야 한다")
    void step1_GeometricMedian() {
        List<Coordinate> participants =
                List.of(
                        new Coordinate(37.5, 127.0),
                        new Coordinate(37.6, 127.1),
                        new Coordinate(37.4, 126.9));

        Coordinate center = geometricMedianCalculator.calculate(participants);

        assertThat(center.latitude()).isBetween(37.4, 37.6);
        assertThat(center.longitude()).isBetween(126.9, 127.1);
    }

    @Test
    @DisplayName("Step 2: Polar Sampling은 적절한 개수의 후보를 생성해야 한다")
    void step2_PolarSampling() {
        Coordinate center = new Coordinate(37.5, 127.0);
        List<Coordinate> participants = List.of(new Coordinate(37.6, 127.1));

        List<Coordinate> candidates = polarSamplingGenerator.generate(center, participants);

        // 로직상 최소 20개 이상 생성되도록 설계됨
        assertThat(candidates.size()).isGreaterThanOrEqualTo(20);
        assertThat(candidates).contains(center);
    }

    @Test
    @DisplayName("Step 3: 점수 산정(Scorer)은 이동 시간 편차가 작은 장소에 높은 순위(낮은 점수)를 주어야 한다")
    void step3_Scoring() {
        // given
        // 후보 A: 평균 30분, 모두 30분 (편차 0) -> 더 공정함
        FilteredCandidate candidateA = createFilteredCandidate(37.5, 127.0, List.of(30.0, 30.0));
        // 후보 B: 평균 30분, 한명 10분/한명 50분 (편차 큼)
        FilteredCandidate candidateB = createFilteredCandidate(37.6, 127.1, List.of(10.0, 50.0));

        // when
        List<ScoredCandidate> result = candidateScorer.score(List.of(candidateA, candidateB), 2);

        // then
        assertThat(result.get(0).filteredCandidate()).isEqualTo(candidateA); // A가 1위(낮은 점수)여야 함
        assertThat(result.get(0).score()).isLessThan(result.get(1).score());
    }

    private FilteredCandidate createFilteredCandidate(double lat, double lon, List<Double> times) {
        Coordinate coord = new Coordinate(lat, lon);
        List<CandidateFilter.ParticipantTravelTime> pTimes =
                times.stream()
                        .map(
                                t ->
                                        new CandidateFilter.ParticipantTravelTime(
                                                new Coordinate(0, 0), t))
                        .toList();
        return new FilteredCandidate(coord, pTimes);
    }
}
