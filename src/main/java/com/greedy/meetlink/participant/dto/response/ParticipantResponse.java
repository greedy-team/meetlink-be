package com.greedy.meetlink.participant.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.greedy.meetlink.participant.entity.Participant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParticipantResponse {
    private final String nickname;
    private final String token;
    private final Boolean isHost;
    private final Boolean isTimeSubmitted;
    private final Boolean isPlaceSubmitted;

    public static ParticipantResponse of(Participant participant) {
        return ParticipantResponse.builder()
                .nickname(participant.getNickname())
                .token(participant.getToken())
                .isHost(participant.isHost())
                .build();
    }

    public static ParticipantResponse of(
            Participant participant, boolean isTimeSubmitted, boolean isPlaceSubmitted) {
        return ParticipantResponse.builder()
                .nickname(participant.getNickname())
                .isHost(participant.isHost())
                .isTimeSubmitted(isTimeSubmitted)
                .isPlaceSubmitted(isPlaceSubmitted)
                .build();
    }
}
