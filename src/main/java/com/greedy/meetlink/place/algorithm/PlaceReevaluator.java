package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.algorithm.CandidateFilter.ParticipantTravelTime;
import com.greedy.meetlink.place.algorithm.CandidateScorer.ScoredCandidate;
import com.greedy.meetlink.place.client.PoiClient;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse.PoiPlace;
import com.greedy.meetlink.place.domain.Coordinate;
import com.greedy.meetlink.place.domain.PlaceSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 실제 장소 매핑 (리팩토링 버전)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceReevaluator {

    // [수정] 구체 클래스 대신 인터페이스 사용 (Real/Fake 교체 가능)
    private final PoiClient poiClient;

    /**
     * 상위 후보 좌표를 실제 장소(POI)와 매핑
     */
    public List<ReevaluatedPlace> matchWithRealPlaces(List<ScoredCandidate> topCandidates) {
        List<ReevaluatedPlace> results = new ArrayList<>();
        int rank = 1;

        for (ScoredCandidate candidate : topCandidates) {
            Coordinate coord = candidate.filteredCandidate().coordinate();

            // 1. POI 검색 (인터페이스 호출)
            List<PoiPlace> places = poiClient.searchNearby(coord);
            
            PlaceSearchResult searchResult;
            if (places.isEmpty()) {
                searchResult = new PlaceSearchResult(
                        "추천 중간 지점 " + rank,
                        "상세 주소 없음 (" + coord.latitude() + ", " + coord.longitude() + ")",
                        coord
                );
            } else {
                PoiPlace place = places.get(0);
                searchResult = PlaceSearchResult.from(place);
            }

            // 2. 이동 시간 정보 매핑
            List<Double> travelTimes = candidate.filteredCandidate().participantTravelTimes().stream()
                    .map(ParticipantTravelTime::travelTimeMinutes)
                    .collect(Collectors.toList());

            results.add(new ReevaluatedPlace(
                    searchResult,
                    travelTimes,
                    candidate.avgTravelTime(),
                    candidate.maxTravelTime(),
                    candidate.score(),
                    rank++
            ));
        }

        return results;
    }

    public record ReevaluatedPlace(
            PlaceSearchResult searchResult,
            List<Double> travelTimesMinutes,
            double avgTravelTime,
            double maxTravelTime,
            double score,
            int rank
    ) {}
}
