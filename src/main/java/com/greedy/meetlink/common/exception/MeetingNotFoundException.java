package com.greedy.meetlink.common.exception;

public class MeetingNotFoundException extends RuntimeException {

    public MeetingNotFoundException(Long id) {
        super("모임을 찾을 수 없습니다. ID: " + id);
    }
}
