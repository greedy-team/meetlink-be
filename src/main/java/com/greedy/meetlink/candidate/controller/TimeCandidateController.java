package com.greedy.meetlink.candidate.controller;

import com.greedy.meetlink.candidate.dto.response.TimeCandidateResponse;
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
@RequestMapping("/meetings/{code}")
public class TimeCandidateController {
    private final TimeCandidateService timeCandidateService;

    @PostMapping("/candidates/time")
    public ApiResponse<List<TimeCandidateResponse>> calculate(@PathVariable String code) {
        return ApiResponse.success(timeCandidateService.calculate(code));
    }

    @GetMapping("/candidates/time")
    public ApiResponse<List<TimeCandidateResponse>> list(@PathVariable String code) {
        return ApiResponse.success(timeCandidateService.list(code));
    }
}
