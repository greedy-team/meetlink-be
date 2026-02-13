package com.greedy.meetlink.result.entity;

public record ParticipantDetail(
        String nickname,
        double travelTime,
        String routeData
) {
    public static ParticipantDetail from(PlaceTravelInfo info) {
        return new ParticipantDetail(
                info.getParticipant().getNickname(),
                info.getTravelTime(),
                info.getRouteData()
        );
    }
}
