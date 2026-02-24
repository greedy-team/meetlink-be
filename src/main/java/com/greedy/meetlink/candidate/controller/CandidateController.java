package com.greedy.meetlink.candidate.controller;

import com.greedy.meetlink.candidate.dto.response.PlaceCandidateResponse;
import com.greedy.meetlink.candidate.dto.response.TimeCandidateResponse;
import com.greedy.meetlink.candidate.service.PlaceCandidateService;
import com.greedy.meetlink.candidate.service.TimeCandidateService;
import com.greedy.meetlink.common.ApiResponse;
import java.util.List;
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
    private final PlaceCandidateService placeCandidateService;

    @PostMapping("/time")
    public ApiResponse<List<TimeCandidateResponse>> calculateTime(@PathVariable String code) {
        return ApiResponse.success(timeCandidateService.calculate(code));
    }

    @GetMapping("/time")
    public ApiResponse<List<TimeCandidateResponse>> getTimeCandidates(@PathVariable String code) {
        return ApiResponse.success(timeCandidateService.list(code));
    }

    @PostMapping("/place")
    public ApiResponse<List<PlaceCandidateResponse>> calculatePlace(@PathVariable String code) {
        return ApiResponse.success(placeCandidateService.calculate(code));
    }

    @GetMapping("/place")
    public ApiResponse<List<PlaceCandidateResponse>> getPlaceCandidates(@PathVariable String code) {
        return ApiResponse.success(placeCandidateService.list(code));
    }
}
