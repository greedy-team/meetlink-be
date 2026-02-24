package com.greedy.meetlink.common.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MotisRouteResponse(
        Place from,
        Place to,
        List<Itinerary> direct,
        List<Itinerary> itineraries,
        String previousPageCursor,
        String nextPageCursor) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Itinerary(
            Long duration,
            String startTime,
            String endTime,
            Integer transfers,
            Double distance,
            Double elevationGain,
            Double elevationLoss,
            List<Leg> legs) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Leg(
            String startTime,
            String endTime,
            Place from,
            Place to,
            String mode,
            Double distance,
            String polyline,
            String tripId,
            Boolean realTime,
            Boolean interlineWithPreviousLeg) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Place(
            String name,
            String stopId,
            Double lat,
            Double lon,
            Double level,
            String arrival,
            String departure,
            String scheduledArrival,
            String scheduledDeparture,
            String track,
            String scheduledTrack,
            String vertexType) {}
}
