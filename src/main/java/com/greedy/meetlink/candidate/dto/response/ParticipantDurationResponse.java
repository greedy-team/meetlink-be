package com.greedy.meetlink.candidate.dto.response;

import com.greedy.meetlink.candidate.entity.PlaceTravelInfo;

public record ParticipantDurationResponse(String nickname, int duration, String pathData) {
    public static ParticipantDurationResponse from(PlaceTravelInfo travelInfo) {
        return new ParticipantDurationResponse(
                travelInfo.getParticipant().getNickname(),
                (int) (travelInfo.getTravelTime() * 60), // 분 → 초
                travelInfo.getRouteData());
    }
}
