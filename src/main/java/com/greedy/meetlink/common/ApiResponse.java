package com.greedy.meetlink.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean status;
    private final T result;
    private final String code;
    private final String message;

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(true, result, null, null);
    }

    public static <T> ApiResponse<T> error(ResponseCode code) {
        return error(code, code.getMessage());
    }

    public static <T> ApiResponse<T> error(ResponseCode code, T result) {
        return new ApiResponse<>(false, result, code.name(), code.getMessage());
    }

    public static <T> ApiResponse<T> error(ResponseCode code, String message) {
        return new ApiResponse<>(false, null, code.name(), message);
    }
}
