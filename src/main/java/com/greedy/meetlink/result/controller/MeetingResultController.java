package com.greedy.meetlink.result.controller;

import com.greedy.meetlink.common.ApiResponse;
import com.greedy.meetlink.result.dto.response.MeetingResultResponse;
import com.greedy.meetlink.result.service.MeetingResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetings/{code}/result")
public class MeetingResultController implements MeetingResultControllerSpec {
    private final MeetingResultService meetingResultService;

    @GetMapping
    public ApiResponse<MeetingResultResponse> getMeetingResult(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token) {
        return ApiResponse.success(meetingResultService.getMeetingResult(code, token));
    }
}
