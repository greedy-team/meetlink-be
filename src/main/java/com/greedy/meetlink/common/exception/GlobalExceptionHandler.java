package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.dto.response.ErrorResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j // 로깅을 위한 어노테이션 추가
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 비즈니스 예외 (MeetingNotFoundException) */
    @ExceptionHandler(MeetingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMeetingNotFoundException(
            MeetingNotFoundException ex) {
        log.warn("Meeting not found: {}", ex.getMessage());

        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /** Bean Validation 유효성 검증 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(
                        error -> {
                            String fieldName = ((FieldError) error).getField();
                            String errorMessage = error.getDefaultMessage();
                            errors.put(fieldName, errorMessage);
                        });

        log.warn("Validation failed: {}", errors);

        return createErrorResponse(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다.", errors);
    }

    /** 잘못된 인자 전달 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /** 서버 내부 오류 (예상치 못한 예외) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected server error occurred: ", ex);

        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", null);
    }

    /** 중복 코드를 줄이기 위한 공통 응답 생성 메서드 */
    private ResponseEntity<ErrorResponse> createErrorResponse(
            HttpStatus status, String message, Map<String, String> errors) {
        ErrorResponse response =
                ErrorResponse.builder()
                        .status(status.value())
                        .message(message)
                        .errors(errors)
                        .build();
        return ResponseEntity.status(status).body(response);
    }
}
