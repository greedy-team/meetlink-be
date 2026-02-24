package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.candidate.algorithm.CandidateFilter.FilteredCandidate;
import com.greedy.meetlink.candidate.algorithm.CandidateFilter.ParticipantTravelTime;
import com.greedy.meetlink.candidate.algorithm.ScoreCalculator.ScoreResult;
import com.greedy.meetlink.client.PoiClient;
import com.greedy.meetlink.client.TransitClient;
import com.greedy.meetlink.client.dto.PoiSearchResponse.PoiPlace;
import com.greedy.meetlink.common.Coordinate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 후보 좌표를 카카오 POI 검색으로 실제 장소와 매칭하고, POI 좌표 기준으로 이동시간을 재계산하여 최종 순위를 확정 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceMapper {
    private final PoiClient poiClient;
    private final TransitClient transitClient;
    private final ScoreCalculator scoreCalculator;

    public List<MatchedPlace> match(List<FilteredCandidate> candidates) {
        List<MatchedPlace> results = new ArrayList<>();

        for (int i = 0; i < candidates.size(); i++) {
            FilteredCandidate candidate = candidates.get(i);
            Coordinate coord = candidate.coordinate();
            List<ParticipantTravelTime> originalTimes = candidate.participantTravelTimes();

            List<PoiPlace> places = poiClient.searchNearby(coord);

            String name;
            String address;
            Coordinate poiCoord;
            List<Double> travelTimes;

            if (places.isEmpty()) {
                log.debug(
                        "[6-{}] No POI found near ({}, {}), using raw coordinate",
                        i + 1,
                        coord.latitude(),
                        coord.longitude());
                name = "추천 중간 지점 " + (i + 1);
                address =
                        String.format("상세 주소 없음 (%.4f, %.4f)", coord.latitude(), coord.longitude());
                poiCoord = coord;
                travelTimes = extractTravelTimes(originalTimes);
            } else {
                PoiPlace place = places.get(0);
                name = place.name();
                address = place.address();
                poiCoord = new Coordinate(place.latitude(), place.longitude());
                log.debug("[6-{}] POI matched: {} / {}", i + 1, name, address);

                travelTimes = recalculateTravelTimes(originalTimes, poiCoord);
            }

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
                            0));
        }

        results.sort(Comparator.comparingDouble(MatchedPlace::score));
        for (int i = 0; i < results.size(); i++) {
            results.set(i, results.get(i).withRank(i + 1));
        }

        return results;
    }

    private List<Double> recalculateTravelTimes(
            List<ParticipantTravelTime> originalTimes, Coordinate poiCoord) {
        List<Double> times = new ArrayList<>();

        for (ParticipantTravelTime ptt : originalTimes) {
            Double recalcTime =
                    transitClient.getTravelTimeSeconds(ptt.participantCoordinate(), poiCoord);
            if (recalcTime == null) {
                log.warn(
                        "POI travel time recalculation failed, using original value: poi={}",
                        poiCoord);
            }
            times.add(recalcTime != null ? recalcTime : ptt.travelTimeSeconds());
        }

        return times;
    }

    private List<Double> extractTravelTimes(List<ParticipantTravelTime> participantTravelTimes) {
        return participantTravelTimes.stream()
                .map(ParticipantTravelTime::travelTimeSeconds)
                .collect(Collectors.toList());
    }

    public record MatchedPlace(
            String name,
            String address,
            Coordinate coordinate,
            double avgTravelTime,
            double maxTravelTime,
            double score,
            int rank) {
        public MatchedPlace withRank(int rank) {
            return new MatchedPlace(
                    name, address, coordinate, avgTravelTime, maxTravelTime, score, rank);
        }
    }
}
