package com.greedy.meetlink.candidate.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

        log.info(
                "Place candidate calculation started: meeting={}, participants={}",
                code,
                participants.size());

        List<Coordinate> coordinates =
                locationAvailabilityRepository.findByParticipantIn(participants).stream()
                        .map(la -> new Coordinate(la.getLatitude(), la.getLongitude()))
                        .toList();

        List<MatchedPlace> matchedPlaces = computeRecommendedPlaces(coordinates);

        if (matchedPlaces.isEmpty()) {
            throw new PlaceRecommendationFailedException();
        }

        placeCandidateRepository.deleteByMeeting(meeting);
        List<PlaceCandidate> savedCandidates = saveCandidates(meeting, matchedPlaces);
        updateMeetingResult(meeting, savedCandidates.getFirst());

        log.info(
                "Place candidate calculation completed: meeting={}, results={}",
                code,
                savedCandidates.size());

        return savedCandidates.stream().map(PlaceCandidateResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PlaceCandidateResponse> list(String code) {
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

    private List<MatchedPlace> computeRecommendedPlaces(List<Coordinate> coordinates) {
        log.debug("[1] Participants: {} locations", coordinates.size());

        Coordinate center = geometricMedianCalculator.calculate(coordinates);
        log.debug("[2] Geometric median: ({}, {})", center.latitude(), center.longitude());

        List<Coordinate> rawCandidates = polarSamplingGenerator.generate(center, coordinates);
        log.debug("[3] Polar sampling: {} raw candidates", rawCandidates.size());

        double realDMax = coordinates.stream().mapToDouble(center::distanceTo).max().orElse(0.0);
        List<Coordinate> distanceFiltered =
                candidateFilter.filterByDistance(rawCandidates, coordinates, realDMax);
        log.debug(
                "[4] Distance filter: {} → {} candidates (dMax: {} km)",
                rawCandidates.size(),
                distanceFiltered.size(),
                String.format("%.3f", realDMax));

        List<CandidateFilter.FilteredCandidate> scored =
                candidateScorer.selectTop(distanceFiltered, coordinates, TOP_K);
        log.debug("[5] Travel time + top {}: {} candidates remaining", TOP_K, scored.size());

        List<MatchedPlace> results = placeMapper.match(scored);
        log.debug(
                "[6] Final results: {}",
                results.stream()
                        .map(p -> p.name() + "(score=" + String.format("%.1f", p.score()) + ")")
                        .toList());

        return results;
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
                                                mp.maxTravelTime()))
                        .toList();

        for (int i = 0; i < candidates.size(); i++) {
            candidates.get(i).assignRank(i + 1);
        }

        return placeCandidateRepository.saveAll(candidates);
    }
}
