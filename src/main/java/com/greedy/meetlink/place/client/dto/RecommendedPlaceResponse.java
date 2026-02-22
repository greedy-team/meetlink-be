package com.greedy.meetlink.place.client.dto;

import com.greedy.meetlink.candidate.PlaceCandidate;
import com.greedy.meetlink.place.domain.PlaceTravelInfo;

import java.util.List;

/**
 * 추천 장소 단건 응답 DTO
 *
 * averageDuration / maxDuration: 초 단위
 *   (PlaceCandidate.avgTravelTime / maxTravelTime은 분 단위이므로 × 60 변환)
 */
public record RecommendedPlaceResponse(
        String placeName,
        String roadAddress,
        double latitude,
        double longitude,
        int rank,
        int averageDuration,
        int maxDuration,
        List<ParticipantDurationResponse> participantDurations
) {
    public static RecommendedPlaceResponse of(
            PlaceCandidate candidate,
            List<PlaceTravelInfo> travelInfos) {

        List<ParticipantDurationResponse> participantDurations = travelInfos.stream()
                .map(ParticipantDurationResponse::from)
                .toList();

        return new RecommendedPlaceResponse(
                candidate.getName(),
                candidate.getAddress(),
                candidate.getLatitude(),
                candidate.getLongitude(),
                candidate.getRank(),
                (int) (candidate.getAvgTravelTime() * 60),  // 분 → 초
                (int) (candidate.getMaxTravelTime() * 60),  // 분 → 초
                participantDurations
        );
    }
}
