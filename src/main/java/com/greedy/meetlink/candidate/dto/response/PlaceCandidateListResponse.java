package com.greedy.meetlink.candidate.dto.response;

import java.util.List;

public record PlaceCandidateListResponse(List<RecommendedPlaceResponse> recommendedPlaces) {
    public static PlaceCandidateListResponse of(List<RecommendedPlaceResponse> places) {
        return new PlaceCandidateListResponse(places);
    }
}