package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.algorithm.CandidateScorer.ScoredCandidate;
import com.greedy.meetlink.place.algorithm.ScoreCalculator.ScoreResult;
import com.greedy.meetlink.place.client.TMapPoiClient;
import com.greedy.meetlink.place.client.TMapTransitClient;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse.PoiPlace;
import com.greedy.meetlink.place.domain.Coordinate;
import com.greedy.meetlink.place.domain.PlaceSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 실제 장소 기준 재평가 (8단계)
 *
 * 후보 좌표 주변의 실제 장소(POI)를 검색하고,
 * 그 장소들을 기준으로 다시 이동 시간을 계산하여 최종 순위를 매깁니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceReevaluator {

    private final TMapPoiClient tMapPoiClient;
    private final TMapTransitClient tMapTransitClient;
    private final ScoreCalculator scoreCalculator;

    /**
     * 상위 K개 후보 좌표 → 실제 장소 검색 → 재평가 → 최종 순위 반환
     *
     * @param topCandidates     점수 산정 상위 K개 후보 (좌표 기준)
     * @param participantCoords 전체 참여자 출발지 좌표 목록
     * @param finalTopK         최종 반환할 후보 수
     * @return 실제 장소 기준으로 재평가된 최종 후보 목록
     */
    public List<ReevaluatedPlace> reevaluate(
            List<ScoredCandidate> topCandidates,
            List<Coordinate> participantCoords,
            int finalTopK) {

        List<ReevaluatedPlace> allReevaluated = new ArrayList<>();

        for (ScoredCandidate candidate : topCandidates) {
            Coordinate coord = candidate.filteredCandidate().coordinate();

            // 1. 후보 좌표 주변 실제 장소 POI 검색
            List<PoiPlace> places = tMapPoiClient.searchNearby(coord);

            if (places.size() > 1) {
                places = places.subList(0, 1); // [비용 절감] 상위 1개만 평가
            }

            if (places.isEmpty()) {
                log.warn("[재평가] POI 검색 결과 없음: coord={}", coord);
                continue;
            }

            // 2. 각 실제 장소에 대해 전체 참여자 이동시간 재계산
            for (PoiPlace place : places) {
                // 이미 평가된 장소인지 확인하는 로직이 필요할 수 있음 (중복 장소 제거)
                // 하지만 여기서는 리스트에 다 담고 나중에 정렬/제한으로 처리
                
                Optional<ReevaluatedPlace> reevaluated =
                        evaluatePlace(place, participantCoords);

                reevaluated.ifPresent(allReevaluated::add);
            }
        }

        if (allReevaluated.isEmpty()) {
            return List.of();
        }

        // 3. 점수 기준 오름차순 정렬 → 중복 제거 (이름+주소 기준) → 최종 상위 K개 선정
        List<ReevaluatedPlace> sorted = allReevaluated.stream()
                .sorted(Comparator.comparingDouble(ReevaluatedPlace::score))
                // 중복 제거: 같은 장소가 여러 후보 좌표에서 검색되었을 수 있음
                .collect(Collectors.toMap(
                        p -> p.searchResult().uniqueId(),
                        p -> p,
                        (existing, replacement) -> existing // 이미 존재하는(점수가 더 낮거나 같은) 것 유지
                ))
                .values().stream()
                .sorted(Comparator.comparingDouble(ReevaluatedPlace::score))
                .limit(finalTopK)
                .collect(Collectors.toList());

        // 4. 최종 순위 부여
        for (int i = 0; i < sorted.size(); i++) {
            sorted.set(i, sorted.get(i).withRank(i + 1));
        }

        log.info("[재평가] 최종 후보 {}개 확정", sorted.size());
        return sorted;
    }

    /**
     * 단일 실제 장소에 대해 전체 참여자 이동시간 계산 및 점수 산정
     */
    private Optional<ReevaluatedPlace> evaluatePlace(
            PoiPlace place,
            List<Coordinate> participantCoords) {

        PlaceSearchResult searchResult = PlaceSearchResult.from(place);
        Coordinate placeCoord = searchResult.coordinate();
        List<Double> travelTimes = new ArrayList<>();

        for (Coordinate participant : participantCoords) {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            Double travelTime = tMapTransitClient.getTravelTimeMinutes(participant, placeCoord);

            if (travelTime == null) {
                log.debug("[재평가] 이동시간 조회 실패: place={}, participant={}", place.name(), participant);
                return Optional.empty();
            }

            travelTimes.add(travelTime);
        }

        ScoreResult scoreResult = scoreCalculator.calculate(travelTimes);

        return Optional.of(new ReevaluatedPlace(
                searchResult,
                travelTimes,
                scoreResult.avg(),
                scoreResult.max(),
                scoreResult.score(),
                0   // 순위는 나중에 부여
        ));
    }

    /**
     * 실제 장소 기준으로 재평가된 결과
     */
    public record ReevaluatedPlace(
            PlaceSearchResult searchResult,
            List<Double> travelTimesMinutes,
            double avgTravelTime,
            double maxTravelTime,
            double score,
            int rank
    ) {
        public ReevaluatedPlace withRank(int rank) {
            return new ReevaluatedPlace(
                    searchResult, travelTimesMinutes, avgTravelTime, maxTravelTime, score, rank);
        }
    }
}
