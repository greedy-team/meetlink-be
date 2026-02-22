package com.greedy.meetlink.candidate.service;

import com.greedy.meetlink.candidate.dto.response.PlaceCandidateListResponse;
import com.greedy.meetlink.candidate.dto.response.RecommendedPlaceResponse;
import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.candidate.entity.PlaceTravelInfo;
import com.greedy.meetlink.candidate.repository.PlaceCandidateRepository;
import com.greedy.meetlink.candidate.repository.PlaceTravelInfoRepository;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceCandidateQueryService {

    private final MeetingRepository meetingRepository;
    private final PlaceCandidateRepository placeCandidateRepository;
    private final PlaceTravelInfoRepository placeTravelInfoRepository;

    public PlaceCandidateListResponse getCandidates(String code) {
        Meeting meeting =
                meetingRepository.findByCode(code).orElseThrow(MeetingNotFoundException::new);

        List<PlaceCandidate> candidates =
                placeCandidateRepository.findByMeetingOrderByRankAsc(meeting);

        if (candidates.isEmpty()) {
            return PlaceCandidateListResponse.of(List.of());
        }

        List<PlaceTravelInfo> allTravelInfos =
                placeTravelInfoRepository.findByPlaceCandidatesWithParticipant(candidates);

        Map<Long, List<PlaceTravelInfo>> travelInfoMap =
                allTravelInfos.stream()
                        .collect(Collectors.groupingBy(pti -> pti.getPlaceCandidate().getId()));

        List<RecommendedPlaceResponse> recommendedPlaces =
                candidates.stream()
                        .map(
                                candidate ->
                                        RecommendedPlaceResponse.of(
                                                candidate,
                                                travelInfoMap.getOrDefault(
                                                        candidate.getId(), List.of())))
                        .toList();

        return PlaceCandidateListResponse.of(recommendedPlaces);
    }
}
