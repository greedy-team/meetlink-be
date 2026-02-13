package com.greedy.meetlink.result.dto;

import com.greedy.meetlink.candidate.PlaceCandidate;
import com.greedy.meetlink.result.entity.ParticipantDetail;
import lombok.Builder;

import java.util.List;

@Builder
public record PlaceRecommendationResponse(
        String name,
        String address,
        double latitude,
        double longitude,
        int rank,
        double avgTravelTime,
        double maxTravelTime,
        List<ParticipantDetail> participants // 참여자 소요 시간 리스트
) {
    public static PlaceRecommendationResponse from(PlaceCandidate candidate) {
        return new PlaceRecommendationResponse(
                candidate.getName(),
                candidate.getAddress(),
                candidate.getLatitude(),
                candidate.getLongitude(),
                candidate.getRank(),
                candidate.getAvgTravelTime(),
                candidate.getMaxTravelTime(),
                candidate.getTravelInfos().stream()
                        .map(ParticipantDetail::from)
                        .toList()
        );
    }
}

