package com.greedy.meetlink.meeting.controller;

import com.greedy.meetlink.common.ApiResponse;
import com.greedy.meetlink.meeting.dto.request.MeetingCreateRequest;
import com.greedy.meetlink.meeting.dto.request.MeetingUpdateRequest;
import com.greedy.meetlink.meeting.dto.response.MeetingResponse;
import com.greedy.meetlink.meeting.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetings")
public class MeetingController implements MeetingControllerSpec {
    private final MeetingService meetingService;

    @GetMapping("/{code}")
    public ApiResponse<MeetingResponse> get(@PathVariable String code) {
        MeetingResponse response = meetingService.get(code);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<MeetingResponse> create(@Valid @RequestBody MeetingCreateRequest request) {
        MeetingResponse response = meetingService.create(request);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{code}")
    public ApiResponse<MeetingResponse> update(
            @PathVariable String code,
            @RequestHeader("X-Participant-Token") String token,
            @Valid @RequestBody MeetingUpdateRequest request) {
        MeetingResponse response = meetingService.update(code, token, request);
        return ApiResponse.success(response);
    }
}
