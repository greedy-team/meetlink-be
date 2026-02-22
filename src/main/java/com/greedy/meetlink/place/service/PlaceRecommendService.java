package com.greedy.meetlink.place.service;

import com.greedy.meetlink.candidate.PlaceCandidate;
import com.greedy.meetlink.candidate.PlaceCalculationType;
import com.greedy.meetlink.candidate.PlaceCandidateRepository;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import com.greedy.meetlink.place.algorithm.CandidateFilter;
import com.greedy.meetlink.place.algorithm.CandidateScorer;
import com.greedy.meetlink.place.algorithm.GeometricMedianCalculator;
import com.greedy.meetlink.place.algorithm.PlaceMapper;
import com.greedy.meetlink.place.algorithm.PlaceMapper.MatchedPlace;
import com.greedy.meetlink.place.algorithm.PolarSamplingGenerator;
import com.greedy.meetlink.place.domain.Coordinate;
import com.greedy.meetlink.place.domain.PlaceTravelInfo;
import com.greedy.meetlink.place.repository.PlaceTravelInfoRepository;
import com.greedy.meetlink.result.entity.MeetingResult;
import com.greedy.meetlink.result.repository.MeetingResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 장소 추천 서비스 (오케스트레이터)
 *
 * ✅ 트랜잭션 전략:
 *   - runAlgorithm()     : @Transactional 없음  → TMap API 호출(최대 수분)이 포함되어
 *                          DB 커넥션을 장시간 점유하지 않도록 분리
 *   - saveResults()      : @Transactional      → 모든 DB 저장을 하나의 트랜잭션으로 묶어
 *                          알고리즘 성공 후 부분 저장 방지
 *   - recommendAndSave() : 두 메서드를 순서대로 호출하는 public 진입점
 *
 * ✅ PlaceCandidate ↔ MeetingResult 연결 전략:
 *   - PlaceCandidate는 TOP_K(3)개 전부 저장 → 사용자에게 후보 선택지 제공
 *   - MeetingResult.placeCandidate는 rank=1(최적 장소)만 연결 → 최종 결과는 1개
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceRecommendService {

    private static final int TOP_K = 3;

    // 알고리즘 컴포넌트
    private final GeometricMedianCalculator geometricMedianCalculator;
    private final PolarSamplingGenerator    polarSamplingGenerator;
    private final CandidateFilter           candidateFilter;
    private final CandidateScorer           candidateScorer;
    private final PlaceMapper               placeMapper;

    // 저장소
    private final PlaceCandidateRepository placeCandidateRepository;
    private final PlaceTravelInfoRepository placeTravelInfoRepository;
    private final MeetingResultRepository   meetingResultRepository;
    private final ParticipantRepository     participantRepository;

    // -------------------------------------------------------------------------
    // public API
    // -------------------------------------------------------------------------

    /**
     * 장소 추천 실행 → DB 저장 → MeetingResult 연결
     *
     * @param meeting 장소 추천을 실행할 미팅
     */
    public void recommendAndSave(Meeting meeting) {
        List<Participant> participants = participantRepository.findByMeeting(meeting);
        validateParticipants(participants);

        // 1단계: 알고리즘 실행 (트랜잭션 없음 — API 호출 포함)
        List<MatchedPlace> matchedPlaces = runAlgorithm(participants);

        if (matchedPlaces.isEmpty()) {
            throw new IllegalStateException(
                    "meetingId=" + meeting.getId() + " : 추천 가능한 장소를 찾지 못했습니다.");
        }

        // 2단계: DB 저장 (단일 트랜잭션)
        saveResults(meeting, participants, matchedPlaces);
    }

    // -------------------------------------------------------------------------
    // 알고리즘 실행 (트랜잭션 없음)
    // -------------------------------------------------------------------------

    /**
     * TMap API 호출을 포함한 알고리즘 전체 실행
     * @Transactional을 걸지 않아 DB 커넥션을 API 대기 시간 동안 점유하지 않음
     */
    private List<MatchedPlace> runAlgorithm(List<Participant> participants) {
        List<Coordinate> coordinates = toCoordinates(participants);

        // 기하중심 계산
        Coordinate center = geometricMedianCalculator.calculate(coordinates);
        log.info("기하중심 계산 완료: {}", center);

        // 후보 좌표 생성
        List<Coordinate> rawCandidates = polarSamplingGenerator.generate(center, coordinates);
        log.info("후보 좌표 생성: {}개", rawCandidates.size());

        // 1차 필터: 직선 거리
        double realDMax = coordinates.stream().mapToDouble(center::distanceTo).max().orElse(0.0);
        List<Coordinate> distanceFiltered = candidateFilter.filterByDistance(rawCandidates, coordinates, realDMax);
        log.info("1차 거리 필터 후: {}개", distanceFiltered.size());

        // 2차 필터: 이동시간 (TMap API 호출)
        List<CandidateFilter.FilteredCandidate> timeFiltered =
                candidateFilter.filterByTravelTime(distanceFiltered, coordinates, center);
        log.info("2차 이동시간 필터 후: {}개", timeFiltered.size());

        // 점수 산정 + topK 추출
        List<CandidateScorer.ScoredCandidate> scored = candidateScorer.score(timeFiltered, TOP_K);
        log.info("점수 산정 완료: {}개", scored.size());

        // POI 매핑
        return placeMapper.match(scored);
    }

    // -------------------------------------------------------------------------
    // DB 저장 (단일 트랜잭션)
    // -------------------------------------------------------------------------

    /**
     * PlaceCandidate, PlaceTravelInfo, MeetingResult를 하나의 트랜잭션으로 저장
     *
     * 저장 순서:
     *   1. PlaceCandidate (rank 1~3)
     *   2. PlaceTravelInfo (참여자 × 후보 수)
     *   3. MeetingResult.placeCandidate = rank 1 후보
     */
    @Transactional
    public void saveResults(Meeting meeting, List<Participant> participants, List<MatchedPlace> matchedPlaces) {

        List<PlaceCandidate> savedCandidates = savePlaceCandidates(meeting, matchedPlaces);
        savePlaceTravelInfos(savedCandidates, participants, matchedPlaces);
        linkToMeetingResult(meeting, savedCandidates.get(0)); // rank=1
    }

    /**
     * PlaceCandidate 저장 (rank 1~TOP_K 전부)
     */
    private List<PlaceCandidate> savePlaceCandidates(Meeting meeting, List<MatchedPlace> matchedPlaces) {
        List<PlaceCandidate> saved = new ArrayList<>();

        for (MatchedPlace mp : matchedPlaces) {
            PlaceCandidate candidate = PlaceCandidate.builder()
                    .meeting(meeting)
                    .name(mp.searchResult().name())
                    .address(mp.searchResult().address())
                    .latitude(mp.searchResult().coordinate().latitude())
                    .longitude(mp.searchResult().coordinate().longitude())
                    .avgTravelTime(mp.avgTravelTime())
                    .maxTravelTime(mp.maxTravelTime())
                    .rank(mp.rank())
                    .calculationType(PlaceCalculationType.FAIR)
                    .build();

            saved.add(placeCandidateRepository.save(candidate));
            log.info("PlaceCandidate 저장: rank={}, name={}", mp.rank(), mp.searchResult().name());
        }

        return saved;
    }

    /**
     * PlaceTravelInfo 저장 (참여자별 이동시간)
     * saved[i] ↔ matchedPlaces[i] ↔ participants[j] 인덱스 매핑
     */
    private void savePlaceTravelInfos(
            List<PlaceCandidate> savedCandidates,
            List<Participant> participants,
            List<MatchedPlace> matchedPlaces) {

        for (int i = 0; i < savedCandidates.size(); i++) {
            PlaceCandidate candidate  = savedCandidates.get(i);
            List<Double>   travelTimes = matchedPlaces.get(i).travelTimesMinutes();

            for (int j = 0; j < participants.size(); j++) {
                PlaceTravelInfo travelInfo = PlaceTravelInfo.builder()
                        .placeCandidate(candidate)
                        .participant(participants.get(j))
                        .travelTime(travelTimes.get(j))
                        .build();

                placeTravelInfoRepository.save(travelInfo);
            }
        }
    }

    /**
     * MeetingResult에 rank=1 PlaceCandidate 연결
     * MeetingResult가 없으면 새로 생성 (findOrCreate 패턴)
     */
    private void linkToMeetingResult(Meeting meeting, PlaceCandidate topCandidate) {
        MeetingResult meetingResult = meetingResultRepository.findByMeeting(meeting)
                .orElseGet(() -> meetingResultRepository.save(new MeetingResult(meeting)));

        meetingResult.updatePlaceCandidate(topCandidate);
        // @Transactional → Dirty Checking으로 별도 save() 불필요

        log.info("MeetingResult 연결 완료: meetingId={}, placeCandidate={}",
                meeting.getId(), topCandidate.getName());
    }

    // -------------------------------------------------------------------------
    // utils
    // -------------------------------------------------------------------------

    private void validateParticipants(List<Participant> participants) {
        if (participants == null || participants.size() < 2) {
            throw new IllegalArgumentException("장소 추천을 위해 참여자가 2명 이상 필요합니다.");
        }

        // 좌표 미등록 참여자 검사
        List<String> missingLocations = participants.stream()
                .filter(p -> !p.hasLocation())
                .map(Participant::getNickname)
                .toList();

        if (!missingLocations.isEmpty()) {
            throw new IllegalStateException(
                    "출발지를 등록하지 않은 참여자가 있습니다: " + missingLocations);
        }
    }

    private List<Coordinate> toCoordinates(List<Participant> participants) {
        return participants.stream()
                .map(p -> new Coordinate(p.getLatitude(), p.getLongitude()))
                .toList();
    }
}
