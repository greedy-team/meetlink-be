package com.greedy.meetlink.place.controller;

import com.greedy.meetlink.common.ApiResponse;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.place.client.dto.PlaceCandidateListResponse;
import com.greedy.meetlink.place.service.PlaceCandidateQueryService;
import com.greedy.meetlink.place.service.PlaceRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetings/{code}/candidates/place")
public class PlaceCandidateController implements PlaceCandidateControllerSpec {

    private final PlaceRecommendService placeRecommendService;
    private final PlaceCandidateQueryService placeCandidateQueryService;
    private final MeetingRepository meetingRepository;

    @Override
    @PostMapping
    public ApiResponse<Void> calculate(@PathVariable String code) {
        Meeting meeting = meetingRepository.findByCode(code)
                .orElseThrow(MeetingNotFoundException::new);

        placeRecommendService.recommendAndSave(meeting);
        return ApiResponse.success();
    }

    @Override
    @GetMapping
    public ApiResponse<PlaceCandidateListResponse> getCandidates(@PathVariable String code) {
        Meeting meeting = meetingRepository.findByCode(code)
                .orElseThrow(MeetingNotFoundException::new);

        return ApiResponse.success(placeCandidateQueryService.getCandidates(meeting));
    }
}
