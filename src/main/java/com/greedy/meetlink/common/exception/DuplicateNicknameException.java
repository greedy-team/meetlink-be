package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ResponseCode;

public class DuplicateNicknameException extends AppException {
    public DuplicateNicknameException() {
        super(ResponseCode.DUPLICATE_NICKNAME);
    }
}
