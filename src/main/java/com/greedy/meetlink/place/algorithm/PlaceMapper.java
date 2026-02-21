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
 * 좌표-장소 매핑 (Place Mapper)
 * 후보 좌표를 실제 장소(POI) 정보와 매칭합니다.
 *
 * ✅ 리팩토링: PlaceMapper 내부에서 rank++ 카운터를 별도 관리하던 방식 제거.
 *    CandidateScorer에서 이미 rank가 부여된 ScoredCandidate.rank()를 그대로 사용.
 *
 * [변경 이유]
 *   - rank가 두 곳(CandidateScorer, PlaceMapper)에서 관리되면
 *     topCandidates 리스트 순서가 바뀌거나 필터링이 추가될 경우 rank 불일치 버그 발생 가능.
 *   - 단일 출처(Single Source of Truth) 원칙: rank는 CandidateScorer에서만 결정.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceMapper {

    private final PoiClient poiClient;

    public List<MatchedPlace> match(List<ScoredCandidate> topCandidates) {
        List<MatchedPlace> results = new ArrayList<>();

        for (ScoredCandidate candidate : topCandidates) {
            Coordinate coord = candidate.filteredCandidate().coordinate();

            // 1. POI 검색
            List<PoiPlace> places = poiClient.searchNearby(coord);

            PlaceSearchResult searchResult;
            if (places.isEmpty()) {
                log.warn("POI 검색 결과 없음: coordinate={}", coord);
                searchResult = fallbackSearchResult(coord, candidate.rank());
            } else {
                searchResult = PlaceSearchResult.from(places.get(0));
            }

            // 2. 이동 시간 정보 매핑
            List<Double> travelTimes = candidate.filteredCandidate().participantTravelTimes().stream()
                    .map(ParticipantTravelTime::travelTimeMinutes)
                    .collect(Collectors.toList());

            results.add(new MatchedPlace(
                    searchResult,
                    travelTimes,
                    candidate.avgTravelTime(),
                    candidate.maxTravelTime(),
                    candidate.score(),
                    candidate.rank()   // ✅ 리팩토링: rank++  → ScoredCandidate.rank() 직접 사용
            ));
        }

        return results;
    }

    /**
     * POI 검색 실패 시 좌표 기반 폴백 장소 생성
     */
    private PlaceSearchResult fallbackSearchResult(Coordinate coord, int rank) {
        String name    = "추천 중간 지점 " + rank;
        String address = String.format("상세 주소 없음 (%.4f, %.4f)",
                coord.latitude(), coord.longitude());
        return new PlaceSearchResult(name, address, coord);
    }

    public record MatchedPlace(
            PlaceSearchResult searchResult,
            List<Double> travelTimesMinutes,
            double avgTravelTime,
            double maxTravelTime,
            double score,
            int rank
    ) {}
}
