package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ResponseCode;

public class InsufficientPermissionException extends AppException {
    public InsufficientPermissionException() {
        super(ResponseCode.INSUFFICIENT_PERMISSION);
    }
}
