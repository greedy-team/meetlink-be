package com.greedy.meetlink.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
    // 400
    INVALID_REQUEST("잘못된 요청입니다."),
    INVALID_REQUEST_BODY("요청 본문이 비어있거나 형식이 잘못되었습니다."),
    VALIDATION_FAILED("요청 값이 올바르지 않습니다."),

    // 401
    UNAUTHORIZED("인증이 필요합니다."),

    // 403
    FORBIDDEN("접근 권한이 없습니다."),

    // 404
    NOT_FOUND("대상을 찾을 수 없습니다."),

    // 500
    INTERNAL_ERROR("서버 내부 오류가 발생했습니다.");

    private final String message;
}
