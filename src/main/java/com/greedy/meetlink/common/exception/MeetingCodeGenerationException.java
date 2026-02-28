package com.greedy.meetlink.common.exception;

public class MeetingCodeGenerationException extends RuntimeException {
    public MeetingCodeGenerationException() {
        super("모임 코드 생성 중 오류가 발생했습니다.");
    }
}
