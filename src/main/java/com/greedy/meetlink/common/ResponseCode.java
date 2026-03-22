package com.greedy.meetlink.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문이 비어있거나 형식이 잘못되었습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "해당 HTTP 메서드는 지원되지 않습니다."),
    CONFLICT(HttpStatus.CONFLICT, "이미 존재하는 데이터이거나 상태가 충돌합니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."),
    MEETING_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "모임 추천 결과를 찾을 수 없습니다."),
    LOCATION_NOT_SUBMITTED(HttpStatus.NOT_FOUND, "아직 장소를 입력하지 않았습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_PARTICIPANT_TOKEN(HttpStatus.FORBIDDEN, "유효하지 않은 참여자 토큰입니다."),
    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "참여자를 찾을 수 없습니다."),
    INVALID_TIME_AVAILABILITY(HttpStatus.BAD_REQUEST, "시간 입력이 올바르지 않습니다."),
    PLACE_CANDIDATE_CALCULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "추천 가능한 장소가 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
