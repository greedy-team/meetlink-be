package com.greedy.meetlink.availability.controller;

import com.greedy.meetlink.availability.dto.request.LocationAvailabilityRequest;
import com.greedy.meetlink.availability.dto.request.TimeAvailabilityRequest;
import com.greedy.meetlink.availability.dto.response.LocationAvailabilityResponse;
import com.greedy.meetlink.availability.dto.response.TimeAvailabilitiesResponse;
import com.greedy.meetlink.availability.dto.response.TimeAvailabilityResponse;
import com.greedy.meetlink.availability.service.LocationAvailabilityService;
import com.greedy.meetlink.availability.service.TimeAvailabilityService;
import com.greedy.meetlink.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetings/{code}/availabilities")
public class AvailabilityController implements AvailabilityControllerSpec {
    private final TimeAvailabilityService timeAvailabilityService;
    private final LocationAvailabilityService locationAvailabilityService;

    @PostMapping("/time")
    public ApiResponse<Void> submitTime(
            @PathVariable String code,
            @RequestHeader("X-Participant-Token") String token,
            @Valid @RequestBody TimeAvailabilityRequest request) {
        timeAvailabilityService.submit(code, token, request);

        return ApiResponse.success();
    }

    @GetMapping("/time")
    public ApiResponse<List<TimeAvailabilitiesResponse>> getTimeAvailabilities(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token) {
        return ApiResponse.success(timeAvailabilityService.getTimeAvailabilities(code, token));
    }

    @GetMapping("/time/me")
    public ApiResponse<List<TimeAvailabilityResponse>> getMyTimeAvailability(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token) {
        return ApiResponse.success(timeAvailabilityService.getMyTimeAvailability(code, token));
    }

    @PostMapping("/location")
    public ApiResponse<Void> submitLocation(
            @PathVariable String code,
            @RequestHeader("X-Participant-Token") String token,
            @Valid @RequestBody LocationAvailabilityRequest request) {
        locationAvailabilityService.submit(code, token, request);

        return ApiResponse.success();
    }

    @GetMapping("/location/me")
    public ApiResponse<LocationAvailabilityResponse> getMyLocationAvailability(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token) {
        return ApiResponse.success(
                locationAvailabilityService.getMyLocationAvailability(code, token));
    }
}
