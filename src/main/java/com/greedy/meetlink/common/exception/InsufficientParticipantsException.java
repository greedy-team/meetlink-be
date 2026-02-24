package com.greedy.meetlink.common.exception;

public class InsufficientParticipantsException extends RuntimeException {
    public InsufficientParticipantsException() {
        super("장소 추천을 위해 참여자가 2명 이상 필요합니다.");
    }
}
