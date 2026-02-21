package com.greedy.meetlink.result.dto;

import com.greedy.meetlink.candidate.PlaceCandidate;
import com.greedy.meetlink.result.entity.ParticipantDetail;
import java.util.Collections;
import java.util.List;
import lombok.Builder;

@Builder
public record PlaceRecommendationResponse(
        String name,
        String address,
        double latitude,
        double longitude,
        int rank,
        double avgTravelTime,
        double maxTravelTime,
        List<ParticipantDetail> participants) {
    public static PlaceRecommendationResponse from(PlaceCandidate candidate) {
        return new PlaceRecommendationResponse(
                candidate.getName(),
                candidate.getAddress(),
                candidate.getLatitude(),
                candidate.getLongitude(),
                candidate.getRank(),
                candidate.getAvgTravelTime(),
                candidate.getMaxTravelTime(),
                candidate.getTravelInfos().stream().map(ParticipantDetail::from).toList());
    }

    public static PlaceRecommendationResponse empty() {
        return new PlaceRecommendationResponse(null, null, 0, 0, 0, 0, 0, Collections.emptyList());
    }
}
