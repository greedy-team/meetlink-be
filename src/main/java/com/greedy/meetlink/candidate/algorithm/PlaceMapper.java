package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.candidate.algorithm.CandidateFilter.FilteredCandidate;
import com.greedy.meetlink.candidate.algorithm.CandidateFilter.ParticipantTravelTime;
import com.greedy.meetlink.candidate.algorithm.ScoreCalculator.ScoreResult;
import com.greedy.meetlink.common.client.PoiClient;
import com.greedy.meetlink.common.client.TransitClient;
import com.greedy.meetlink.common.client.dto.PoiSearchResponse.PoiPlace;
import com.greedy.meetlink.common.client.dto.RouteInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** 후보 좌표를 카카오 POI 검색으로 실제 장소와 매칭하고, POI 좌표 기준으로 이동시간을 재계산하여 최종 순위를 확정 */
@Slf4j
@Component
public class PlaceMapper {
    private final PoiClient poiClient;
    private final TransitClient transitClient;
    private final ScoreCalculator scoreCalculator;
    private final Executor executor;

    public PlaceMapper(
            PoiClient poiClient,
            TransitClient transitClient,
            ScoreCalculator scoreCalculator,
            @Qualifier("candidateCalculationExecutor") Executor executor) {
        this.poiClient = poiClient;
        this.transitClient = transitClient;
        this.scoreCalculator = scoreCalculator;
        this.executor = executor;
    }

    public List<MatchedPlace> match(List<FilteredCandidate> candidates) {
        List<MatchedPlace> results = new ArrayList<>();

        for (int i = 0; i < candidates.size(); i++) {
            FilteredCandidate candidate = candidates.get(i);
            Coordinate coord = candidate.coordinate();
            List<ParticipantTravelTime> originalTimes = candidate.participantTravelTimes();

            List<PoiPlace> places = poiClient.searchNearby(coord.latitude(), coord.longitude());
            if (places.isEmpty()) {
                log.debug(
                        "[6-{}] No POI found near ({}, {}), skipping",
                        i + 1,
                        coord.latitude(),
                        coord.longitude());
                continue;
            }

            PoiPlace place = places.get(0);
            String name = place.name();
            String address = place.address();
            Coordinate poiCoord = new Coordinate(place.latitude(), place.longitude());
            log.debug("[6-{}] POI matched: {} / {}", i + 1, name, address);

            List<RouteInfo> routes = fetchRoutes(originalTimes, poiCoord);
            List<Double> travelTimes =
                    IntStream.range(0, routes.size())
                            .mapToObj(
                                    j ->
                                            routes.get(j) != null
                                                    ? routes.get(j).travelTime()
                                                    : originalTimes.get(j).travelTimeSeconds())
                            .toList();

            ScoreResult scoreResult = scoreCalculator.calculate(travelTimes);
            log.debug(
                    "[6-{}] Score: avg={}s, max={}s, score={}",
                    i + 1,
                    String.format("%.0f", scoreResult.avg()),
                    String.format("%.0f", scoreResult.max()),
                    String.format("%.1f", scoreResult.score()));

            results.add(
                    new MatchedPlace(
                            name,
                            address,
                            poiCoord,
                            scoreResult.avg(),
                            scoreResult.max(),
                            scoreResult.score(),
                            routes));
        }

        results.sort(Comparator.comparingDouble(MatchedPlace::score));
        return results;
    }

    private List<RouteInfo> fetchRoutes(
            List<ParticipantTravelTime> originalTimes, Coordinate poiCoord) {
        List<CompletableFuture<RouteInfo>> futures =
                originalTimes.stream()
                        .map(
                                ptt ->
                                        CompletableFuture.supplyAsync(
                                                () ->
                                                        transitClient.getPlan(
                                                                ptt.participantCoordinate()
                                                                        .latitude(),
                                                                ptt.participantCoordinate()
                                                                        .longitude(),
                                                                poiCoord.latitude(),
                                                                poiCoord.longitude()),
                                                executor))
                        .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream()
                .map(
                        f -> {
                            RouteInfo plan = f.join();
                            if (plan == null) {
                                log.warn(
                                        "POI travel time recalculation failed, using original value: poi={}",
                                        poiCoord);
                            }
                            return plan;
                        })
                .collect(Collectors.toList());
    }

    public record MatchedPlace(
            String name,
            String address,
            Coordinate coordinate,
            double avgTravelTime,
            double maxTravelTime,
            double score,
            List<RouteInfo> routes) {}
}
