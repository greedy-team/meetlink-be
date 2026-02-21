package com.greedy.meetlink.availability.controller;

import com.greedy.meetlink.availability.dto.request.LocationAvailabilityRequest;
import com.greedy.meetlink.availability.dto.request.TimeAvailabilityRequest;
import com.greedy.meetlink.availability.service.LocationAvailabilityService;
import com.greedy.meetlink.availability.service.TimeAvailabilityService;
import com.greedy.meetlink.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetings/{code}")
public class AvailabilityController {
    private final TimeAvailabilityService timeAvailabilityService;
    private final LocationAvailabilityService locationAvailabilityService;

    // 시간 입력
    @PostMapping("/time-availabilities")
    public ApiResponse<Void> submitTime(
            @PathVariable String code,
            @RequestHeader("X-Participant-Token") String token,
            @Valid @RequestBody TimeAvailabilityRequest request) {
        timeAvailabilityService.submit(code, token, request);

        return ApiResponse.success();
    }

    // 위치 입력
    @PostMapping("/location-availabilities")
    public ApiResponse<Void> submitLocation(
            @PathVariable String code,
            @RequestHeader("X-Participant-Token") String token,
            @Valid @RequestBody LocationAvailabilityRequest request) {
        locationAvailabilityService.submit(code, token, request);

        return ApiResponse.success();
    }
}
