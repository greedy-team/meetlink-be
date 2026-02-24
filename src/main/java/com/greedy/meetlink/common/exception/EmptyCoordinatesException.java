package com.greedy.meetlink.common.exception;

public class EmptyCoordinatesException extends RuntimeException {
    public EmptyCoordinatesException() {
        super("좌표 목록이 비어 있습니다.");
    }
}
