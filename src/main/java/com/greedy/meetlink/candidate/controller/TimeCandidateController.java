package com.greedy.meetlink.candidate.controller;

import com.greedy.meetlink.candidate.dto.response.TimeCandidateListResponse;
import com.greedy.meetlink.candidate.service.TimeCandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meetings/{code}")
@RequiredArgsConstructor
public class TimeCandidateController {
    private final TimeCandidateService timeCandidateService;

    @PostMapping("/candidates/time")
    public ResponseEntity<TimeCandidateListResponse> calculate(@PathVariable String code) {
        return ResponseEntity.ok(timeCandidateService.calculateAndSave(code));
    }

    @GetMapping("/candidates/time")
    public ResponseEntity<TimeCandidateListResponse> getCandidates(@PathVariable String code) {
        return ResponseEntity.ok(timeCandidateService.getCandidates(code));
    }
}
