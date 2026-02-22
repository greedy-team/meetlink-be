package com.greedy.meetlink.place.client.dto;

import com.greedy.meetlink.place.domain.PlaceTravelInfo;

/**
 * 참여자별 이동시간 + 경로 응답 DTO
 *
 * duration: 초 단위 (PlaceTravelInfo.travelTime은 분 단위이므로 × 60 변환)
 * pathData: Polyline 등 경로 데이터 (저장된 값 그대로 반환)
 */
public record ParticipantDurationResponse(
        String nickname,
        int duration,
        String pathData
) {
    public static ParticipantDurationResponse from(PlaceTravelInfo travelInfo) {
        return new ParticipantDurationResponse(
                travelInfo.getParticipant().getNickname(),
                (int) (travelInfo.getTravelTime() * 60),  // 분 → 초
                travelInfo.getRouteData()
        );
    }
}
