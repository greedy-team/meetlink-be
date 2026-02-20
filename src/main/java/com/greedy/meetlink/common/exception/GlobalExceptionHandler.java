package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ApiResponse;
import com.greedy.meetlink.common.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MeetingNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleMeetingNotFoundException(
            MeetingNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ResponseCode.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult()
                .getFieldErrors()
                .forEach((error) -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ResponseCode.VALIDATION_FAILED, errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ResponseCode.INVALID_REQUEST_BODY));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoHandlerFoundException() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ResponseCode.NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpRequestMethodNotSupportedException() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(ResponseCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleFallbackException(Exception e) {
        log.error("Unexpected server error occurred: ", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ResponseCode.INTERNAL_ERROR));
    }

    /**
     * 참여자 정보 없음 예외
     */
    @ExceptionHandler(ParticipantNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleParticipantNotFoundException(
            ParticipantNotFoundException e) {
        log.warn("Participant not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ResponseCode.NOT_FOUND, e.getMessage()));
    }

    /**
     * 닉네임 중복 예외
     */
    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateNicknameException(
            DuplicateNicknameException e) {
        log.warn("Duplicate nickname: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ResponseCode.CONFLICT, e.getMessage()));    }
}
