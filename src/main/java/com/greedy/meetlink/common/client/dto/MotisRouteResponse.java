package com.greedy.meetlink.common.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MotisRouteResponse(List<Itinerary> direct, List<Itinerary> itineraries) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Itinerary(Long duration, List<Leg> legs) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Leg(String mode, LegGeometry legGeometry) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LegGeometry(String points) {}
}
