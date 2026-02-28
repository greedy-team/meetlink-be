package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ResponseCode;

public class InvalidParticipantTokenException extends AppException {
    public InvalidParticipantTokenException() {
        super(ResponseCode.INVALID_PARTICIPANT_TOKEN);
    }
}
