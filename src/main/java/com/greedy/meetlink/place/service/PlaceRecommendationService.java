package com.greedy.meetlink.place.service;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.candidate.PlaceCalculationType;
import com.greedy.meetlink.candidate.PlaceCandidate;
import com.greedy.meetlink.candidate.PlaceCandidateRepository;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.place.algorithm.CandidateFilter;
import com.greedy.meetlink.place.algorithm.CandidateFilter.FilteredCandidate;
import com.greedy.meetlink.place.algorithm.CandidateScorer;
import com.greedy.meetlink.place.algorithm.CandidateScorer.ScoredCandidate;
import com.greedy.meetlink.place.algorithm.GeometricMedianCalculator;
import com.greedy.meetlink.place.algorithm.PlaceReevaluator;
import com.greedy.meetlink.place.algorithm.PlaceReevaluator.ReevaluatedPlace;
import com.greedy.meetlink.place.algorithm.PolarSamplingGenerator;
import com.greedy.meetlink.place.domain.Coordinate;
import com.greedy.meetlink.place.domain.PlaceSearchResult;
import com.greedy.meetlink.place.domain.PlaceTravelInfo;
import com.greedy.meetlink.result.PlaceTravelInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 장소 추천 알고리즘 오케스트레이션 (API 최소화 리팩토링 버전)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlaceRecommendationService {

    // 상위 3개 후보 선정
    private static final int TOP_K_COORDINATES = 3;

    private final MeetingRepository meetingRepository;
    private final LocationAvailabilityRepository locationAvailabilityRepository;
    private final PlaceCandidateRepository placeCandidateRepository;
    private final PlaceTravelInfoRepository placeTravelInfoRepository;

    private final GeometricMedianCalculator geometricMedianCalculator;
    private final PolarSamplingGenerator polarSamplingGenerator;
    private final CandidateFilter candidateFilter;
    private final CandidateScorer candidateScorer;
    private final PlaceReevaluator placeReevaluator;

    public void recommend(String meetingCode) {
        Meeting meeting = meetingRepository.findByCode(meetingCode)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다: " + meetingCode));

        List<LocationAvailability> locations = locationAvailabilityRepository.findByMeetingCode(meetingCode);
        if (locations.size() < 2) {
            log.warn("[장소추천] 참여자 부족. meetingCode={}, 참여자 수={}", meetingCode, locations.size());
            return;
        }

        // 1. 참여자 좌표 추출
        List<Coordinate> participantCoords = locations.stream()
                .map(loc -> new Coordinate(loc.getLatitude(), loc.getLongitude()))
                .collect(Collectors.toList());

        // 2. 기하중심 계산
        Coordinate center = geometricMedianCalculator.calculate(participantCoords);

        // 3. 후보 좌표 생성
        List<Coordinate> rawCandidates = polarSamplingGenerator.generate(center, participantCoords);

        // 4. 1차 필터링 (직선 거리)
        double dMax = participantCoords.stream().mapToDouble(center::distanceTo).max().orElse(0.0);
        List<Coordinate> distanceFiltered = candidateFilter.filterByDistance(rawCandidates, participantCoords, dMax);

        // [비용 절감] API 호출 전, 가장 유력한 상위 3개만 선정하여 TMap API 호출
        if (distanceFiltered.size() > TOP_K_COORDINATES) {
            distanceFiltered = distanceFiltered.stream()
                    .sorted(Comparator.comparingDouble(center::distanceTo))
                    .limit(TOP_K_COORDINATES)
                    .collect(Collectors.toList());
        }

        // 5. 2차 필터링 (TMap 이동시간 계산 - API 호출 발생)
        // 여기서 계산된 이동시간을 최종적으로 사용 (재평가 X)
        List<FilteredCandidate> travelTimeFiltered =
                candidateFilter.filterByTravelTime(distanceFiltered, participantCoords, center);

        if (travelTimeFiltered.isEmpty()) {
            log.warn("[장소추천] 필터 통과 후보 없음.");
            return;
        }

        // 6. 점수 산정
        List<ScoredCandidate> topCoordinates = candidateScorer.score(travelTimeFiltered, TOP_K_COORDINATES);

        // 7. [리팩토링] 실제 장소 정보 매핑 (API 재호출 없이 정보만 매핑)
        List<ReevaluatedPlace> finalPlaces = placeReevaluator.matchWithRealPlaces(topCoordinates);

        // 8. 저장
        saveFinalCandidates(meeting, finalPlaces, locations);
    }

    private void saveFinalCandidates(
            Meeting meeting,
            List<ReevaluatedPlace> finalPlaces,
            List<LocationAvailability> locations) {

        placeCandidateRepository.deleteByMeeting(meeting);

        for (ReevaluatedPlace place : finalPlaces) {
            PlaceSearchResult searchResult = place.searchResult();

            PlaceCandidate candidate = PlaceCandidate.builder()
                    .meeting(meeting)
                    .name(searchResult.name())
                    .address(searchResult.address())
                    .latitude(searchResult.coordinate().latitude())
                    .longitude(searchResult.coordinate().longitude())
                    .avgTravelTime(place.avgTravelTime())
                    .maxTravelTime(place.maxTravelTime())
                    .rank(place.rank())
                    .calculationType(PlaceCalculationType.FAIR)
                    .build();

            PlaceCandidate saved = placeCandidateRepository.save(candidate);

            List<Double> travelTimes = place.travelTimesMinutes();
            
            // locations와 travelTimes의 순서가 일치한다고 가정
            // (ParticipantCoords 순서대로 CandidateFilter에서 처리하므로)
            for (int i = 0; i < travelTimes.size() && i < locations.size(); i++) {
                PlaceTravelInfo travelInfo = PlaceTravelInfo.builder()
                        .placeCandidate(saved)
                        .participant(locations.get(i).getParticipant())
                        .travelTime(travelTimes.get(i))
                        .routeData(null)
                        .build();

                placeTravelInfoRepository.save(travelInfo);
            }
        }
    }
}
