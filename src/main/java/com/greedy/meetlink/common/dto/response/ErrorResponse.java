package com.greedy.meetlink.common.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ErrorResponse(
        int status,
        String message,
        Map<String, String> errors
) {}
