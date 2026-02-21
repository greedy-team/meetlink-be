package com.greedy.meetlink.common.exception;

public class MeetingResultNotFoundException extends RuntimeException {
    public MeetingResultNotFoundException(String message) {
        super("모임 추천 결과를 찾을 수 없습니다.");
    }
}
