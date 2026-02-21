package com.greedy.meetlink.common.exception;

public class ParticipantNotFoundException extends RuntimeException {
    public ParticipantNotFoundException() {
        super("참여자 정보를 찾을 수 없습니다.");
    }
}
