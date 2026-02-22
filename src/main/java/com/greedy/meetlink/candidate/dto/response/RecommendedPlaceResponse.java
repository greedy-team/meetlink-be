package com.greedy.meetlink.candidate.dto.response;

import com.greedy.meetlink.candidate.entity.PlaceCandidate;

public record RecommendedPlaceResponse(
        String placeName,
        String roadAddress,
        double latitude,
        double longitude,
        int rank,
        int averageDuration,
        int maxDuration) {

    public static RecommendedPlaceResponse of(PlaceCandidate candidate) {
        return new RecommendedPlaceResponse(
                candidate.getName(),
                candidate.getAddress(),
                candidate.getLatitude(),
                candidate.getLongitude(),
                candidate.getRank(),
                (int) (candidate.getAvgTravelTime() * 60), // 분 → 초
                (int) (candidate.getMaxTravelTime() * 60)); // 분 → 초
    }
}
