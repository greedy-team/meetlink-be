package com.greedy.meetlink.availability.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeAvailabilitiesResponse {
    private final String nickname;
    private final List<TimeAvailabilityResponse> availabilities;
}
