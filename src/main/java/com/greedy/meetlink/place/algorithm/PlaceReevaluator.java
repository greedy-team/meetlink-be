package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.algorithm.CandidateFilter.ParticipantTravelTime;
import com.greedy.meetlink.place.algorithm.CandidateScorer.ScoredCandidate;
import com.greedy.meetlink.place.client.TMapPoiClient;
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
 *
 * 기존 재평가(Re-evaluation) 로직을 제거하고,
 * 이미 계산된 후보 좌표의 이동 시간을 그대로 사용하면서
 * 주변의 실제 장소(POI) 정보(이름, 주소)만 매핑합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceReevaluator {

    private final TMapPoiClient tMapPoiClient;

    /**
     * 상위 후보 좌표를 실제 장소(POI)와 매핑
     * API 재호출 없이 기존 이동 시간 정보를 재사용합니다.
     */
    public List<ReevaluatedPlace> matchWithRealPlaces(List<ScoredCandidate> topCandidates) {
        List<ReevaluatedPlace> results = new ArrayList<>();
        int rank = 1;

        for (ScoredCandidate candidate : topCandidates) {
            Coordinate coord = candidate.filteredCandidate().coordinate();

            // 1. POI 검색 (이름, 주소 획득용)
            List<PoiPlace> places = tMapPoiClient.searchNearby(coord);
            
            PlaceSearchResult searchResult;
            if (places.isEmpty()) {
                // POI가 없으면 그냥 좌표 주소를 사용하거나 "추천 중간 지점"으로 명명
                // 여기서는 간단하게 좌표값을 이름으로 사용하거나 임의의 이름 부여
                searchResult = new PlaceSearchResult(
                        "추천 중간 지점 " + rank,
                        "상세 주소 없음 (" + coord.latitude() + ", " + coord.longitude() + ")",
                        coord
                );
            } else {
                // 가장 가까운 1개 장소 정보만 사용 (POI 좌표로 변경)
                // 주의: 좌표가 바뀌지만 이동 시간은 원래 좌표 기준임 (근사치)
                PoiPlace place = places.get(0);
                searchResult = PlaceSearchResult.from(place);
            }

            // 2. 이동 시간 정보 매핑 (API 호출 없이 기존 값 사용)
            // ScoredCandidate -> FilteredCandidate -> List<ParticipantTravelTime>
            List<Double> travelTimes = candidate.filteredCandidate().participantTravelTimes().stream()
                    .map(ParticipantTravelTime::travelTimeMinutes)
                    .collect(Collectors.toList());

            results.add(new ReevaluatedPlace(
                    searchResult,
                    travelTimes,
                    candidate.avgTravelTime(), // 이미 계산된 평균
                    candidate.maxTravelTime(), // 이미 계산된 최대
                    candidate.score(),         // 이미 계산된 점수
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
