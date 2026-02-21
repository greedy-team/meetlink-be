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

import com.greedy.meetlink.result.MeetingResultRepository;
import com.greedy.meetlink.result.PlaceTravelInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 장소 추천 알고리즘 오케스트레이션 (리팩토링 버전)
 *
 * ┌─────────────────────────────────────────────────────┐
 * │ 실행 순서                                            │
 * │  1. 참여자 출발지 좌표 조회                          │
 * │  2. Weiszfeld → 기하중심 계산                       │
 * │  3. Polar Sampling → 후보 좌표 생성                 │
 * │  4. 1차 필터링 (직선 거리)                          │
 * │  5. 2차 필터링 (TMap 이동시간 - 효율화 적용)          │
 * │  6. 점수 산정 → 상위 K개 좌표 선정                  │
 * │  7. 실제 장소 검색 및 재평가 (PlaceReevaluator 위임) │
 * │  8. PlaceCandidate + PlaceTravelInfo 저장            │
 * └─────────────────────────────────────────────────────┘
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlaceRecommendationService {

    private static final int TOP_K_COORDINATES = 5;
    private static final int FINAL_TOP_K = 5;

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
        // 1단계: 모임 및 참여자 출발지 조회
        Meeting meeting = meetingRepository.findByCode(meetingCode)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다: " + meetingCode));

        List<LocationAvailability> locations = locationAvailabilityRepository.findByMeetingCode(meetingCode);
        if (locations.size() < 2) {
            log.warn("[장소추천] 참여자 부족. meetingCode={}, 참여자 수={}", meetingCode, locations.size());
            return;
        }

        // locations 순서를 그대로 유지 → participantCoords[i] === locations[i].getParticipant()
        List<Coordinate> participantCoords = locations.stream()
                .map(loc -> new Coordinate(loc.getLatitude(), loc.getLongitude()))
                .collect(Collectors.toList());

        log.info("[장소추천] 시작. meetingCode={}, 참여자 수={}", meetingCode, participantCoords.size());

        // 2단계: Weiszfeld 기하중심 계산
        Coordinate center = geometricMedianCalculator.calculate(participantCoords);
        log.info("[장소추천] 기하중심: lat={}, lon={}", center.latitude(), center.longitude());

        // 3단계: Polar Sampling 후보 좌표 생성
        List<Coordinate> rawCandidates = polarSamplingGenerator.generate(center, participantCoords);
        log.info("[장소추천] 초기 후보 수={}", rawCandidates.size());

        // 4단계: 1차 필터링 (직선 거리)
        double dMax = participantCoords.stream()
                .mapToDouble(center::distanceTo).max().orElse(0.0);
        List<Coordinate> distanceFiltered =
                candidateFilter.filterByDistance(rawCandidates, participantCoords, dMax);
        log.info("[장소추천] 1차 필터 후 후보 수={}", distanceFiltered.size());

        // [비용 절감] TMap API 호출 전, 직선 거리 기준 상위 1개로 제한 (API 쿼터 고려)
        if (distanceFiltered.size() > 1) {
            distanceFiltered = distanceFiltered.stream()
                    .sorted(java.util.Comparator.comparingDouble(center::distanceTo))
                    .limit(1)
                    .collect(Collectors.toList());
            log.info("[장소추천] API 쿼터 절약을 위해 최우선 후보 1곳만 상세 조회 진행");
        }

        // 5단계: 2차 필터링 (TMap 이동시간)
        List<FilteredCandidate> travelTimeFiltered =
                candidateFilter.filterByTravelTime(distanceFiltered, participantCoords, center);
        log.info("[장소추천] 2차 필터 후 후보 수={}", travelTimeFiltered.size());

        if (travelTimeFiltered.isEmpty()) {
            log.warn("[장소추천] 필터 통과 후보 없음. meetingCode={}", meetingCode);
            return;
        }

        // 6단계: 점수 산정 → 상위 K개 좌표 선정
        List<ScoredCandidate> topCoordinates = candidateScorer.score(travelTimeFiltered, TOP_K_COORDINATES);
        log.info("[장소추천] 상위 {}개 좌표 선정 완료", topCoordinates.size());

        // 7단계: 실제 장소 검색 및 재평가 (PlaceReevaluator 위임)
        List<ReevaluatedPlace> finalPlaces =
                placeReevaluator.reevaluate(topCoordinates, participantCoords, FINAL_TOP_K);
        log.info("[장소추천] 재평가 완료. 최종 후보 수={}", finalPlaces.size());

        if (finalPlaces.isEmpty()) {
            log.warn("[장소추천] 재평가 통과 장소 없음. meetingCode={}", meetingCode);
            return;
        }

        // 8단계: PlaceCandidate + PlaceTravelInfo 저장
        saveFinalCandidates(meeting, finalPlaces, locations);
        log.info("[장소추천] 완료. meetingCode={}", meetingCode);
    }

    /**
     * 최종 후보 저장
     * locations[i].getParticipant() ↔ travelTimesMinutes[i] 1:1 매핑.
     * participantCoords가 locations와 동일 순서로 생성되었으므로 인덱스가 보장됨.
     */
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
            for (int i = 0; i < travelTimes.size(); i++) {
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
