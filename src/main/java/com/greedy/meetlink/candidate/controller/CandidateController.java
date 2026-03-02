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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetings/{code}/candidates")
public class CandidateController implements CandidateControllerSpec {
    private final TimeCandidateService timeCandidateService;
    private final PlaceCandidateService placeCandidateService;

    @GetMapping("/time")
    public ApiResponse<List<TimeCandidateResponse>> getTimeCandidates(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token) {
        return ApiResponse.success(timeCandidateService.getTimeCandidates(code, token));
    }

    @GetMapping("/place")
    public ApiResponse<List<PlaceCandidateResponse>> getPlaceCandidates(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token) {
        return ApiResponse.success(placeCandidateService.getPlaceCandidates(code, token));
    }
}
