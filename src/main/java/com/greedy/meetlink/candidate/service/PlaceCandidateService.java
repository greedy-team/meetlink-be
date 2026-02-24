package com.greedy.meetlink.candidate.service;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.candidate.algorithm.CandidateFilter;
import com.greedy.meetlink.candidate.algorithm.CandidateScorer;
import com.greedy.meetlink.candidate.algorithm.GeometricMedianCalculator;
import com.greedy.meetlink.candidate.algorithm.PlaceMapper;
import com.greedy.meetlink.candidate.algorithm.PlaceMapper.MatchedPlace;
import com.greedy.meetlink.candidate.algorithm.PolarSamplingGenerator;
import com.greedy.meetlink.candidate.dto.response.PlaceCandidateResponse;
import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.candidate.repository.PlaceCandidateRepository;
import com.greedy.meetlink.common.Coordinate;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.common.exception.PlaceRecommendationFailedException;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import com.greedy.meetlink.result.entity.MeetingResult;
import com.greedy.meetlink.result.repository.MeetingResultRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceCandidateService {
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
    public List<PlaceCandidateResponse> calculate(String code) {
        Meeting meeting =
                meetingRepository.findByCode(code).orElseThrow(MeetingNotFoundException::new);

        List<Participant> participants = participantRepository.findByMeeting(meeting);

        if (!isCalculationRequired(meeting, participants)) {
            return toResponses(meeting);
        }

        Map<Long, LocationAvailability> locationMap = loadLocationMap(participants);

        List<MatchedPlace> matchedPlaces = computeRecommendedPlaces(participants, locationMap);

        if (matchedPlaces.isEmpty()) {
            throw new PlaceRecommendationFailedException();
        }

        placeCandidateRepository.deleteByMeeting(meeting);
        List<PlaceCandidate> savedCandidates = saveCandidates(meeting, matchedPlaces);
        updateMeetingResult(meeting, savedCandidates.getFirst());

        return savedCandidates.stream().map(PlaceCandidateResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PlaceCandidateResponse> get(String code) {
        Meeting meeting =
                meetingRepository.findByCode(code).orElseThrow(MeetingNotFoundException::new);
        return toResponses(meeting);
    }

    private List<PlaceCandidateResponse> toResponses(Meeting meeting) {
        return placeCandidateRepository.findByMeetingOrderByRankAsc(meeting).stream()
                .map(PlaceCandidateResponse::from)
                .toList();
    }

    private boolean isCalculationRequired(Meeting meeting, List<Participant> participants) {
        if (participants.size() < 2) return false;

        List<Long> submittedIds =
                locationAvailabilityRepository.findSubmittedParticipantIds(meeting);
        if (submittedIds.size() != participants.size()) return false;

        return participantRepository
                .findLastLocationSubmission(meeting)
                .map(
                        lastSubmission ->
                                placeCandidateRepository
                                        .findLastCalculatedAt(meeting)
                                        .map(lastSubmission::isAfter)
                                        .orElse(true))
                .orElse(false);
    }

    private void updateMeetingResult(Meeting meeting, PlaceCandidate topCandidate) {
        MeetingResult result = meetingResultRepository.findByMeeting(meeting).orElseThrow();
        result.updatePlaceCandidate(topCandidate);
    }

    private Map<Long, LocationAvailability> loadLocationMap(List<Participant> participants) {
        return locationAvailabilityRepository.findByParticipantIn(participants).stream()
                .collect(Collectors.toMap(la -> la.getParticipant().getId(), la -> la));
    }

    private List<MatchedPlace> computeRecommendedPlaces(
            List<Participant> participants, Map<Long, LocationAvailability> locationMap) {
        List<Coordinate> coordinates = toCoordinates(participants, locationMap);

        Coordinate center = geometricMedianCalculator.calculate(coordinates);
        List<Coordinate> rawCandidates = polarSamplingGenerator.generate(center, coordinates);

        double realDMax = coordinates.stream().mapToDouble(center::distanceTo).max().orElse(0.0);
        List<Coordinate> distanceFiltered =
                candidateFilter.filterByDistance(rawCandidates, coordinates, realDMax);

        List<CandidateFilter.FilteredCandidate> timeFiltered =
                candidateFilter.filterByTravelTime(distanceFiltered, coordinates, center);

        List<CandidateFilter.FilteredCandidate> scored =
                candidateScorer.selectTop(timeFiltered, TOP_K);

        return placeMapper.match(scored);
    }

    private List<PlaceCandidate> saveCandidates(Meeting meeting, List<MatchedPlace> matchedPlaces) {
        List<PlaceCandidate> candidates =
                matchedPlaces.stream()
                        .map(
                                mp ->
                                        PlaceCandidate.create(
                                                meeting,
                                                mp.name(),
                                                mp.address(),
                                                mp.coordinate().latitude(),
                                                mp.coordinate().longitude(),
                                                mp.avgTravelTime(),
                                                mp.maxTravelTime(),
                                                mp.rank()))
                        .toList();
        return placeCandidateRepository.saveAll(candidates);
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
