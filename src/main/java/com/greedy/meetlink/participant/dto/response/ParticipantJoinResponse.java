package com.greedy.meetlink.participant.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ParticipantJoinResponse {
    private boolean success;

    public ParticipantJoinResponse(boolean success) {
        this.success = success;
    }
}
