package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ResponseCode;

public class ParticipantNotFoundException extends AppException {
    public ParticipantNotFoundException() {
        super(ResponseCode.PARTICIPANT_NOT_FOUND);
    }
}
