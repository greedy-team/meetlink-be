package com.greedy.meetlink.participant.dto.response;

import com.greedy.meetlink.participant.entity.Participant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipantInfoResponse {
    private final Long participantId;
    private final String nickname;
    private final boolean isTimeSubmitted;
    private final boolean isPlaceSubmitted;

    public static ParticipantInfoResponse of(Participant participant, boolean isTimeSubmitted, boolean isPlaceSubmitted) {
    return ParticipantInfoResponse.builder()
                .participantId(participant.getId())
                .nickname(participant.getNickname())
                .isTimeSubmitted(isTimeSubmitted)
                .isPlaceSubmitted(isPlaceSubmitted)
                .build();
    }
}
