package com.greedy.meetlink.candidate.service;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.candidate.PlaceCalculationType;
import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.candidate.repository.PlaceCandidateRepository;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import com.greedy.meetlink.place.algorithm.CandidateFilter;
import com.greedy.meetlink.place.algorithm.CandidateScorer;
import com.greedy.meetlink.place.algorithm.GeometricMedianCalculator;
import com.greedy.meetlink.place.algorithm.PlaceMapper;
import com.greedy.meetlink.place.algorithm.PlaceMapper.MatchedPlace;
import com.greedy.meetlink.place.algorithm.PolarSamplingGenerator;
import com.greedy.meetlink.place.domain.Coordinate;
import com.greedy.meetlink.result.entity.MeetingResult;
import com.greedy.meetlink.result.repository.MeetingResultRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceRecommendService {

    private static final int TOP_K = 3;

    private final GeometricMedianCalculator geometricMedianCalculator;
    private final PolarSamplingGenerator polarSamplingGenerator;
    private final CandidateFilter candidateFilter;
    private final CandidateScorer candidateScorer;
    private final PlaceMapper placeMapper;

    private final MeetingRepository meetingRepository;
    private final PlaceCandidateRepository placeCandidateRepository;
    private final MeetingResultRepository meetingResultRepository;
    private final ParticipantRepository participantRepository;
    private final LocationAvailabilityRepository locationAvailabilityRepository;

    @Transactional
    public void recommendAndSave(String code) {
        Meeting meeting =
                meetingRepository.findByCode(code).orElseThrow(MeetingNotFoundException::new);

        List<Participant> participants = participantRepository.findByMeeting(meeting);

        if (participants == null || participants.size() < 2) {
            throw new IllegalArgumentException("장소 추천을 위해 참여자가 2명 이상 필요합니다.");
        }

        Map<Long, LocationAvailability> locationMap = loadLocationMap(participants);
        validateLocations(participants, locationMap);

        List<MatchedPlace> matchedPlaces = runAlgorithm(participants, locationMap);

        if (matchedPlaces.isEmpty()) {
            throw new IllegalStateException(
                    "meetingId=" + meeting.getId() + " : 추천 가능한 장소를 찾지 못했습니다.");
        }

        saveResults(meeting, matchedPlaces);
    }

    private Map<Long, LocationAvailability> loadLocationMap(List<Participant> participants) {
        return locationAvailabilityRepository.findByParticipantIn(participants).stream()
                .collect(Collectors.toMap(la -> la.getParticipant().getId(), la -> la));
    }

    private void validateLocations(
            List<Participant> participants, Map<Long, LocationAvailability> locationMap) {
        List<String> missingLocations =
                participants.stream()
                        .filter(p -> !locationMap.containsKey(p.getId()))
                        .map(Participant::getNickname)
                        .toList();

        if (!missingLocations.isEmpty()) {
            throw new IllegalStateException("출발지를 등록하지 않은 참여자가 있습니다: " + missingLocations);
        }
    }

    private List<MatchedPlace> runAlgorithm(
            List<Participant> participants, Map<Long, LocationAvailability> locationMap) {
        List<Coordinate> coordinates = toCoordinates(participants, locationMap);

        Coordinate center = geometricMedianCalculator.calculate(coordinates);
        log.info("기하중심 계산 완료: {}", center);

        List<Coordinate> rawCandidates = polarSamplingGenerator.generate(center, coordinates);
        log.info("후보 좌표 생성: {}개", rawCandidates.size());

        double realDMax = coordinates.stream().mapToDouble(center::distanceTo).max().orElse(0.0);
        List<Coordinate> distanceFiltered =
                candidateFilter.filterByDistance(rawCandidates, coordinates, realDMax);
        log.info("1차 거리 필터 후: {}개", distanceFiltered.size());

        List<CandidateFilter.FilteredCandidate> timeFiltered =
                candidateFilter.filterByTravelTime(distanceFiltered, coordinates, center);
        log.info("2차 이동시간 필터 후: {}개", timeFiltered.size());

        List<CandidateScorer.ScoredCandidate> scored = candidateScorer.score(timeFiltered, TOP_K);
        log.info("점수 산정 완료: {}개", scored.size());

        return placeMapper.match(scored);
    }

    private void saveResults(Meeting meeting, List<MatchedPlace> matchedPlaces) {
        List<PlaceCandidate> savedCandidates = savePlaceCandidates(meeting, matchedPlaces);
        linkToMeetingResult(meeting, savedCandidates.get(0));
    }

    private List<PlaceCandidate> savePlaceCandidates(
            Meeting meeting, List<MatchedPlace> matchedPlaces) {
        List<PlaceCandidate> saved = new ArrayList<>();

        for (MatchedPlace mp : matchedPlaces) {
            PlaceCandidate candidate =
                    PlaceCandidate.builder()
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

    private void linkToMeetingResult(Meeting meeting, PlaceCandidate topCandidate) {
        MeetingResult meetingResult =
                meetingResultRepository
                        .findByMeeting(meeting)
                        .orElseGet(() -> meetingResultRepository.save(new MeetingResult(meeting)));

        meetingResult.updatePlaceCandidate(topCandidate);

        log.info(
                "MeetingResult 연결 완료: meetingId={}, placeCandidate={}",
                meeting.getId(),
                topCandidate.getName());
    }

    private List<Coordinate> toCoordinates(
            List<Participant> participants, Map<Long, LocationAvailability> locationMap) {
        return participants.stream()
                .map(
                        p -> {
                            LocationAvailability loc = locationMap.get(p.getId());
                            return new Coordinate(loc.getLatitude(), loc.getLongitude());
                        })
                .toList();
    }
}
