package com.greedy.meetlink.participant.controller;

import com.greedy.meetlink.participant.dto.request.ParticipantJoinRequest;
import com.greedy.meetlink.participant.dto.response.ParticipantInfoResponse;
import com.greedy.meetlink.participant.dto.response.ParticipantJoinResponse;
import com.greedy.meetlink.participant.service.ParticipantService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
public class ParticipantController {
    private final ParticipantService participantService;

    // 모임 참여
    @PostMapping
    public ResponseEntity<ParticipantJoinResponse> joinMeeting(
            @PathVariable String code, @RequestBody ParticipantJoinRequest request) {
        return ResponseEntity.ok(participantService.join(code, request));
    }

    // 참여자 목록 조회
    @GetMapping
    public ResponseEntity<List<ParticipantInfoResponse>> getParticipants(
            @PathVariable String code) {
        return ResponseEntity.ok(participantService.getParticipants(code));
    }

    // 내 참여 상태 확인
    @GetMapping("/me")
    public ResponseEntity<ParticipantInfoResponse> getMyStatus(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token) {
        return ResponseEntity.ok(participantService.getMyStatus(code, token));
    }

    // 모임 나가기
    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveMeeting(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token) {
        participantService.leave(code, token);
        return ResponseEntity.noContent().build();
    }
}
