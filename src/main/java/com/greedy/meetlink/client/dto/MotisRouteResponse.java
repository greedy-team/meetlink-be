package com.greedy.meetlink.client.dto;

import java.util.List;

/**
 * MOTIS GET /api/v5/plan 응답 DTO
 *
 * <p>duration 단위: 초 / itineraries 비어 있으면 direct(도보) 사용
 */
public record MotisRouteResponse(List<Itinerary> itineraries, List<Itinerary> direct) {

    public record Itinerary(long duration) {}

    public Double extractMinTravelTimeSeconds() {
        if (itineraries != null && !itineraries.isEmpty()) {
            return (double) itineraries.get(0).duration();
        }
        if (direct != null && !direct.isEmpty()) {
            return (double) direct.get(0).duration();
        }
        return null;
    }
}
