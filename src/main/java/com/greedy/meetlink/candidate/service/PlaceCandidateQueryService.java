package com.greedy.meetlink.candidate.service;

import com.greedy.meetlink.candidate.dto.response.PlaceCandidateListResponse;
import com.greedy.meetlink.candidate.dto.response.RecommendedPlaceResponse;
import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.candidate.repository.PlaceCandidateRepository;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceCandidateQueryService {

    private final MeetingRepository meetingRepository;
    private final PlaceCandidateRepository placeCandidateRepository;

    public PlaceCandidateListResponse getCandidates(String code) {
        Meeting meeting =
                meetingRepository.findByCode(code).orElseThrow(MeetingNotFoundException::new);

        List<PlaceCandidate> candidates =
                placeCandidateRepository.findByMeetingOrderByRankAsc(meeting);

        List<RecommendedPlaceResponse> recommendedPlaces =
                candidates.stream().map(RecommendedPlaceResponse::of).toList();

        return PlaceCandidateListResponse.of(recommendedPlaces);
    }
}
