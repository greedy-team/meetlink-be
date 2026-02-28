package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ResponseCode;

public class MeetingCodeGenerationException extends AppException {
    public MeetingCodeGenerationException() {
        super(ResponseCode.MEETING_CODE_GENERATION_FAILED);
    }
}
