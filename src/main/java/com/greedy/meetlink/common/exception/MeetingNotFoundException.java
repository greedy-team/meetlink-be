package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ResponseCode;

public class MeetingNotFoundException extends AppException {
    public MeetingNotFoundException() {
        super(ResponseCode.MEETING_NOT_FOUND);
    }
}
