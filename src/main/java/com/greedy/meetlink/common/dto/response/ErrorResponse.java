package com.greedy.meetlink.common.dto.response;

import java.util.Map;
import lombok.Builder;

@Builder
public record ErrorResponse(int status, String message, Map<String, String> errors) {}
