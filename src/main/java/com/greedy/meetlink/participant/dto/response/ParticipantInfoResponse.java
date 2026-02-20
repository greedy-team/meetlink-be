package com.greedy.meetlink.participant.dto.response;

import com.greedy.meetlink.participant.entity.Participant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipantInfoResponse {
    private Long participantId;
    private String nickname;
    private boolean isTimeSubmitted;
    private boolean isPlaceSubmitted;

    public static ParticipantInfoResponse from(Participant participant) {
        return ParticipantInfoResponse.builder()
                .participantId(participant.getId())
                .nickname(participant.getNickname())
                .isTimeSubmitted(!participant.getTimeAvailability().isEmpty())
                .isPlaceSubmitted(participant.getStartPoint() != null)
                .build();
    }
}
