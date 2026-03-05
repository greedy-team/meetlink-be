package com.greedy.meetlink.candidate.dto.response;

import com.greedy.meetlink.candidate.entity.PlaceCandidateRoute;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParticipantRouteResponse {
    private final String nickname;
    private final double travelTime;
    private final List<SegmentResponse> segments;

    public static ParticipantRouteResponse from(PlaceCandidateRoute route) {
        List<SegmentResponse> segmentResponses =
                route.getSegments() == null
                        ? List.of()
                        : route.getSegments().stream().map(SegmentResponse::from).toList();
        return ParticipantRouteResponse.builder()
                .nickname(route.getParticipant().getNickname())
                .travelTime(route.getTravelTime())
                .segments(segmentResponses)
                .build();
    }
}
