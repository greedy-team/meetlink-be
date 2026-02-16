package com.greedy.meetlink.meeting.controller;

import com.greedy.meetlink.common.ApiResponse;
import com.greedy.meetlink.meeting.dto.request.MeetingCreateRequest;
import com.greedy.meetlink.meeting.dto.request.MeetingUpdateRequest;
import com.greedy.meetlink.meeting.dto.response.MeetingResponse;
import com.greedy.meetlink.meeting.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;

    @GetMapping("/{code}")
    public ApiResponse<MeetingResponse> getMeeting(@PathVariable String code) {
        MeetingResponse response = meetingService.get(code);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<MeetingResponse> createMeeting(
            @Valid @RequestBody MeetingCreateRequest request) {
        MeetingResponse response = meetingService.create(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{code}")
    public ApiResponse<MeetingResponse> updateMeeting(
            @PathVariable String code, @Valid @RequestBody MeetingUpdateRequest request) {
        MeetingResponse response = meetingService.update(code, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{code}")
    public ApiResponse<Void> deleteMeeting(@PathVariable String code) {
        meetingService.delete(code);
        return ApiResponse.success();
    }
}
