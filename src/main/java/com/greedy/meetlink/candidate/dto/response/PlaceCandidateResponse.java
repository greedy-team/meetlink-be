package com.greedy.meetlink.candidate.dto.response;

import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.candidate.entity.PlaceCandidateRoute;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceCandidateResponse {
    private final String name;
    private final String address;
    private final Double latitude;
    private final Double longitude;
    private final Double avgTravelTime;
    private final Double maxTravelTime;
    private final Integer rank;
    private final List<ParticipantRouteResponse> routes;

    public static PlaceCandidateResponse from(PlaceCandidate candidate) {
        return from(candidate, List.of());
    }

    public static PlaceCandidateResponse from(
            PlaceCandidate candidate, List<PlaceCandidateRoute> routes) {
        return PlaceCandidateResponse.builder()
                .name(candidate.getName())
                .address(candidate.getAddress())
                .latitude(candidate.getLatitude())
                .longitude(candidate.getLongitude())
                .avgTravelTime(candidate.getAvgTravelTime())
                .maxTravelTime(candidate.getMaxTravelTime())
                .rank(candidate.getRank())
                .routes(routes.stream().map(ParticipantRouteResponse::from).toList())
                .build();
    }
}
