package com.greedy.meetlink.candidate.algorithm;

import com.greedy.meetlink.candidate.algorithm.CandidateFilter.ParticipantTravelTime;
import com.greedy.meetlink.candidate.algorithm.CandidateScorer.ScoredCandidate;
import com.greedy.meetlink.candidate.algorithm.ScoreCalculator.ScoreResult;
import com.greedy.meetlink.place.Coordinate;
import com.greedy.meetlink.place.client.PoiClient;
import com.greedy.meetlink.place.client.TransitClient;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse.PoiPlace;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 좌표-장소 매핑 (Place Mapper)
 *
 * <p>후보 좌표를 실제 장소(POI) 정보와 매칭하고, 실제 POI 좌표 기준으로 이동시간을 재계산하여 최종 순위를 확정합니다.
 *
 * <p>[Step 7] 상위 K개 좌표에 대해 POI 검색
 *
 * <p>[Step 8] 실제 POI 좌표 기준으로 이동시간 재계산 → 재정렬 및 rank 재부여
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceMapper {

    private final PoiClient poiClient;
    private final TransitClient transitClient;
    private final ScoreCalculator scoreCalculator;

    public List<MatchedPlace> match(List<ScoredCandidate> topCandidates) {
        List<MatchedPlace> results = new ArrayList<>();

        for (ScoredCandidate candidate : topCandidates) {
            Coordinate coord = candidate.filteredCandidate().coordinate();
            List<ParticipantTravelTime> originalTimes =
                    candidate.filteredCandidate().participantTravelTimes();

            // Step 7: POI 검색
            List<PoiPlace> places = poiClient.searchNearby(coord);

            String name;
            String address;
            Coordinate poiCoord;
            List<Double> travelTimes;

            if (places.isEmpty()) {
                log.warn("POI 검색 결과 없음: coordinate={}", coord);
                name = "추천 중간 지점 " + candidate.rank();
                address =
                        String.format("상세 주소 없음 (%.4f, %.4f)", coord.latitude(), coord.longitude());
                poiCoord = coord;
                travelTimes = toTimeList(originalTimes);
            } else {
                PoiPlace place = places.get(0);
                name = place.name();
                address = place.address();
                poiCoord = new Coordinate(place.latitude(), place.longitude());

                // Step 8: 실제 POI 좌표 기준 이동시간 재계산
                travelTimes = recalculateTravelTimes(originalTimes, poiCoord);
            }

            ScoreResult scoreResult = scoreCalculator.calculate(travelTimes);

            results.add(
                    new MatchedPlace(
                            name,
                            address,
                            poiCoord,
                            travelTimes,
                            scoreResult.avg(),
                            scoreResult.max(),
                            scoreResult.score(),
                            0)); // rank는 재정렬 후 부여
        }

        // Step 8: 재계산된 점수 기준 재정렬 및 rank 재부여
        results.sort(Comparator.comparingDouble(MatchedPlace::score));
        for (int i = 0; i < results.size(); i++) {
            results.set(i, results.get(i).withRank(i + 1));
        }

        return results;
    }

    /**
     * 실제 POI 좌표 기준으로 각 참여자의 이동시간 재계산
     *
     * <p>API 호출 실패 시 원래 이동시간으로 fallback
     */
    private List<Double> recalculateTravelTimes(
            List<ParticipantTravelTime> originalTimes, Coordinate poiCoord) {
        List<Double> times = new ArrayList<>();

        for (ParticipantTravelTime ptt : originalTimes) {
            Double recalcTime = callTransit(ptt.participantCoordinate(), poiCoord);

            if (recalcTime == null) {
                log.warn(
                        "POI 기준 이동시간 재계산 실패 → 원래 값 사용: participant={}, original={}",
                        ptt.participantCoordinate(),
                        ptt.travelTimeMinutes());
                times.add(ptt.travelTimeMinutes());
            } else {
                times.add(recalcTime);
            }
        }

        return times;
    }

    private List<Double> toTimeList(List<ParticipantTravelTime> participantTravelTimes) {
        return participantTravelTimes.stream()
                .map(ParticipantTravelTime::travelTimeMinutes)
                .collect(Collectors.toList());
    }

    private Double callTransit(Coordinate origin, Coordinate destination) {
        return transitClient.getTravelTimeMinutes(origin, destination);
    }

    public record MatchedPlace(
            String name,
            String address,
            Coordinate coordinate,
            List<Double> travelTimesMinutes,
            double avgTravelTime,
            double maxTravelTime,
            double score,
            int rank) {
        public MatchedPlace withRank(int rank) {
            return new MatchedPlace(
                    name,
                    address,
                    coordinate,
                    travelTimesMinutes,
                    avgTravelTime,
                    maxTravelTime,
                    score,
                    rank);
        }
    }
}
