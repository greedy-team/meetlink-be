package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ResponseCode;

public class MeetingResultNotFoundException extends AppException {
    public MeetingResultNotFoundException() {
        super(ResponseCode.MEETING_RESULT_NOT_FOUND);
    }
}
