package com.greedy.meetlink.participant.controller;

import com.greedy.meetlink.common.ApiResponse;
import com.greedy.meetlink.participant.dto.request.HostTransferRequest;
import com.greedy.meetlink.participant.dto.request.ParticipantJoinRequest;
import com.greedy.meetlink.participant.dto.request.PushTokenRequest;
import com.greedy.meetlink.participant.dto.response.ParticipantJoinResponse;
import com.greedy.meetlink.participant.dto.response.ParticipantResponse;
import com.greedy.meetlink.participant.service.ParticipantService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetings/{code}/participants")
public class ParticipantController implements ParticipantControllerSpec {
    private final ParticipantService participantService;

    // 모임 참여
    @PostMapping
    public ApiResponse<ParticipantJoinResponse> join(
            @PathVariable String code, @Valid @RequestBody ParticipantJoinRequest request) {
        return ApiResponse.success(participantService.join(code, request));
    }

    // 참여자 목록 조회
    @GetMapping
    public ApiResponse<List<ParticipantResponse>> getParticipants(
            @PathVariable String code,
            @RequestHeader(value = "X-Participant-Token", required = false) String token) {
        return ApiResponse.success(participantService.getParticipants(code, token));
    }

    // 내 참여 상태 확인
    @GetMapping("/me")
    public ApiResponse<ParticipantResponse> status(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token) {
        return ApiResponse.success(participantService.status(code, token));
    }

    // 모임 나가기
    @DeleteMapping("/me")
    public ApiResponse<Void> leave(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token) {
        participantService.leave(code, token);
        return ApiResponse.success();
    }

    // 푸시 토큰 등록
    @PostMapping("/me/push-token")
    public ApiResponse<Void> registerPushToken(
            @PathVariable String code,
            @RequestHeader("X-Participant-Token") String token,
            @Valid @RequestBody PushTokenRequest request) {
        participantService.savePushToken(code, token, request);
        return ApiResponse.success();
    }

    // 모임장 양도
    @PostMapping("/host")
    public ApiResponse<Void> transferHost(
            @PathVariable String code,
            @RequestHeader("X-Participant-Token") String token,
            @Valid @RequestBody HostTransferRequest request) {
        participantService.transferHost(code, token, request);
        return ApiResponse.success();
    }
}
