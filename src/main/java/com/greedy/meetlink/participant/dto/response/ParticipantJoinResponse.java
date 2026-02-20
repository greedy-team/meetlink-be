package com.greedy.meetlink.participant.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParticipantJoinResponse {
    private final String token;

    public static ParticipantJoinResponse from(String token) {
        return new ParticipantJoinResponse(token);
    }
}
