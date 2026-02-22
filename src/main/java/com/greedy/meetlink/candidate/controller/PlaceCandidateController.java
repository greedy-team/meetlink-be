package com.greedy.meetlink.candidate.controller;

import com.greedy.meetlink.candidate.dto.response.PlaceCandidateListResponse;
import com.greedy.meetlink.candidate.service.PlaceCandidateQueryService;
import com.greedy.meetlink.candidate.service.PlaceRecommendService;
import com.greedy.meetlink.common.ApiResponse;
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

    @Override
    @PostMapping
    public ApiResponse<Void> calculate(@PathVariable String code) {
        placeRecommendService.recommendAndSave(code);
        return ApiResponse.success();
    }

    @Override
    @GetMapping
    public ApiResponse<PlaceCandidateListResponse> getCandidates(@PathVariable String code) {
        return ApiResponse.success(placeCandidateQueryService.getCandidates(code));
    }
}
