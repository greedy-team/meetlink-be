package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.candidate.algorithm.CandidateFilter.FilteredCandidate;
import com.greedy.meetlink.candidate.algorithm.CandidateFilter.ParticipantTravelTime;
import com.greedy.meetlink.common.client.TransitClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** 후보 좌표 이동시간 수집 및 점수 기반 상위 K개 선발 */
@Component
public class CandidateScorer {

    private final TransitClient transitClient;
    private final ScoreCalculator scoreCalculator;
    private final Executor executor;

    public CandidateScorer(
            TransitClient transitClient,
            ScoreCalculator scoreCalculator,
            @Qualifier("candidateCalculationExecutor") Executor executor) {
        this.transitClient = transitClient;
        this.scoreCalculator = scoreCalculator;
        this.executor = executor;
    }

    public List<FilteredCandidate> selectTop(
            List<Coordinate> candidates, List<Coordinate> participants, int topK) {
        List<CompletableFuture<FilteredCandidate>> futures =
                candidates.stream()
                        .map(candidate -> fetchTravelTimesAsync(candidate, participants))
                        .toList();

        return awaitAll(futures).stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(this::calculateScore))
                .limit(topK)
                .collect(Collectors.toList());
    }

    private CompletableFuture<FilteredCandidate> fetchTravelTimesAsync(
            Coordinate candidate, List<Coordinate> participants) {
        List<CompletableFuture<Double>> travelTimeFutures =
                participants.stream()
                        .map(
                                p ->
                                        CompletableFuture.supplyAsync(
                                                () ->
                                                        transitClient.getTravelTime(
                                                                p.latitude(), p.longitude(),
                                                                candidate.latitude(),
                                                                        candidate.longitude()),
                                                executor))
                        .toList();

        return CompletableFuture.allOf(travelTimeFutures.toArray(new CompletableFuture[0]))
                .thenApply(
                        v -> {
                            List<ParticipantTravelTime> times = new ArrayList<>();
                            for (int i = 0; i < participants.size(); i++) {
                                Double travelTime = travelTimeFutures.get(i).join();
                                if (travelTime == null) return null;
                                times.add(
                                        new ParticipantTravelTime(participants.get(i), travelTime));
                            }
                            return new FilteredCandidate(candidate, times);
                        });
    }

    private double calculateScore(FilteredCandidate candidate) {
        List<Double> times =
                candidate.participantTravelTimes().stream()
                        .map(ParticipantTravelTime::travelTimeSeconds)
                        .toList();
        return scoreCalculator.calculate(times).score();
    }

    private static <T> List<T> awaitAll(List<CompletableFuture<T>> futures) {
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return futures.stream().map(CompletableFuture::join).toList();
    }
}
