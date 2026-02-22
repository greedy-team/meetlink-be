package com.greedy.meetlink.place.client.dto;

import java.util.List;

/**
 * GET /meetings/{code}/candidates/place 최상위 응답 DTO
 *
 * <p>{ "recommendedPlaces": [ ... ] }
 */
public record PlaceCandidateListResponse(List<RecommendedPlaceResponse> recommendedPlaces) {
    public static PlaceCandidateListResponse of(List<RecommendedPlaceResponse> places) {
        return new PlaceCandidateListResponse(places);
    }
}
