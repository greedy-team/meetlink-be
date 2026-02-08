package com.greedy.meetlink.meeting;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;

    @GetMapping("/{code}")
    public ResponseEntity<MeetingDetailResponse> getMeetingDetail(@PathVariable String code) {
        MeetingDetailResponse response = meetingService.getMeetingDetail(code);
        return ResponseEntity.ok(response);
    }
}
