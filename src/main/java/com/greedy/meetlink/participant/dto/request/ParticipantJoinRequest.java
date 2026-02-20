package com.greedy.meetlink.participant.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantJoinRequest {
    private String nickname;
    private String token;
}
