package com.greedy.meetlink.common.exception;

public class MeetingCodeGenerationException extends RuntimeException {
    public MeetingCodeGenerationException() {
        super("고유한 모임 코드 생성에 실패했습니다.");
    }
}
