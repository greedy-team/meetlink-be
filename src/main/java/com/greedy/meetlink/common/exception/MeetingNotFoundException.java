package com.greedy.meetlink.common.exception;

public class MeetingNotFoundException extends RuntimeException {

    public MeetingNotFoundException(String code) {
        super("모임을 찾을 수 없습니다: " + code);
    }
}
