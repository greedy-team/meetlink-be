package com.greedy.meetlink.common.exception;

public class MissingParticipantLocationException extends RuntimeException {
    public MissingParticipantLocationException() {
        super("출발지를 등록하지 않은 참여자가 있습니다.");
    }
}
