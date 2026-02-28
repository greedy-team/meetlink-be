package com.greedy.meetlink.common.exception;

public class LocationAvailabilityNotFoundException extends RuntimeException {
    public LocationAvailabilityNotFoundException() {
        super("아직 장소를 입력하지 않았습니다.");
    }
}
