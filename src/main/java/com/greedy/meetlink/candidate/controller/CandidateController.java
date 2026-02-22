package com.greedy.meetlink.candidate.controller;

import com.greedy.meetlink.candidate.dto.response.PlaceCandidateListResponse;
import com.greedy.meetlink.candidate.dto.response.TimeCandidatesResponse;
import com.greedy.meetlink.candidate.service.PlaceCandidateQueryService;
import com.greedy.meetlink.candidate.service.PlaceRecommendService;
import com.greedy.meetlink.candidate.service.TimeCandidateService;
import com.greedy.meetlink.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetings/{code}/candidates")
public class CandidateController implements CandidateControllerSpec {
    private final TimeCandidateService timeCandidateService;
    private final PlaceRecommendService placeRecommendService;
    private final PlaceCandidateQueryService placeCandidateQueryService;

    @PostMapping("/time")
    public ApiResponse<TimeCandidatesResponse> calculateTime(@PathVariable String code) {
        return ApiResponse.success(timeCandidateService.calculate(code));
    }

    @GetMapping("/time")
    public ApiResponse<TimeCandidatesResponse> getTime(@PathVariable String code) {
        return ApiResponse.success(timeCandidateService.get(code));
    }

    @PostMapping("/place")
    public ApiResponse<Void> calculatePlace(@PathVariable String code) {
        placeRecommendService.recommendAndSave(code);
        return ApiResponse.success();
    }

    @GetMapping("/place")
    public ApiResponse<PlaceCandidateListResponse> getPlace(@PathVariable String code) {
        return ApiResponse.success(placeCandidateQueryService.getCandidates(code));
    }
}
