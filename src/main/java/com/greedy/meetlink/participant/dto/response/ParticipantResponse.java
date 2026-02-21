package com.greedy.meetlink.participant.dto.response;

import com.greedy.meetlink.participant.entity.Participant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipantResponse {
    private final Long id;
    private final String nickname;
    private final boolean isTimeSubmitted;
    private final boolean isPlaceSubmitted;

    public static ParticipantResponse of(
            Participant participant, boolean isTimeSubmitted, boolean isPlaceSubmitted) {
        return ParticipantResponse.builder()
                .id(participant.getId())
                .nickname(participant.getNickname())
                .isTimeSubmitted(isTimeSubmitted)
                .isPlaceSubmitted(isPlaceSubmitted)
                .build();
    }
}
