package com.greedy.meetlink.meeting.controller;

import com.greedy.meetlink.meeting.dto.response.MeetingDetailResponse;
import com.greedy.meetlink.meeting.dto.request.MeetingCreateRequest;
import com.greedy.meetlink.meeting.dto.request.MeetingUpdateRequest;
import com.greedy.meetlink.meeting.dto.response.MeetingResponse;
import com.greedy.meetlink.meeting.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    /**
     * 모임 생성
     *
     * @param request 모임 생성 요청
     * @return 생성된 모임 정보
     */
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody MeetingCreateRequest request) {
        MeetingResponse response = meetingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 모임 수정
     *
     * @param code 모임 code
     * @param request 모임 수정 요청
     * @return 수정된 모임 정보
     */
    @PutMapping("/{code}")
    public ResponseEntity<MeetingResponse> updateMeeting(
            @PathVariable String code, @Valid @RequestBody MeetingUpdateRequest request) {
        MeetingResponse response = meetingService.update(code, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<MeetingDetailResponse> getMeetingDetail(@PathVariable String code) {
        MeetingDetailResponse response = meetingService.getMeetingDetail(code);
        return ResponseEntity.ok(response);
    }

    /**
     * 모임 삭제
     *
     * @param code 모임 code
     * @return 204 No Content
     */
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable String code) {
        meetingService.delete(code);
        return ResponseEntity.noContent().build();
    }
}
