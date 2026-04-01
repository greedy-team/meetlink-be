package com.greedy.meetlink.participant.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PushTokenRequest {
    @NotBlank(message = "푸시 토큰은 필수입니다.")
    private String token;
}
