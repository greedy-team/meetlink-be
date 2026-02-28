package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ResponseCode;

public class InvalidTimeAvailabilityException extends AppException {
    public InvalidTimeAvailabilityException(String message) {
        super(ResponseCode.INVALID_TIME_AVAILABILITY, message);
    }
}
