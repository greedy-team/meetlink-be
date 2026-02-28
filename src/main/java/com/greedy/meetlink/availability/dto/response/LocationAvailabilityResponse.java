package com.greedy.meetlink.availability.dto.response;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationAvailabilityResponse {
    private final String address;
    private final double latitude;
    private final double longitude;

    public static LocationAvailabilityResponse from(LocationAvailability availability) {
        return LocationAvailabilityResponse.builder()
                .address(availability.getAddress())
                .latitude(availability.getLatitude())
                .longitude(availability.getLongitude())
                .build();
    }
}
