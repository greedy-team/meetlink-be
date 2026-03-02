package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ResponseCode;
import lombok.Getter;

@Getter
public abstract class AppException extends RuntimeException {
    private final ResponseCode responseCode;

    protected AppException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    protected AppException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }
}
