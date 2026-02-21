package com.greedy.meetlink.common.exception;

public class InvalidParticipantTokenException extends RuntimeException {
    public InvalidParticipantTokenException() {
        super("유효하지 않은 참여자 토큰입니다.");
    }
}
