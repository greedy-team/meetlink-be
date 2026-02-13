package com.greedy.meetlink.result.controller;

import com.greedy.meetlink.result.dto.PlaceRecommendationResponse;
import com.greedy.meetlink.result.dto.TimeRecommendationResponse;
import com.greedy.meetlink.result.service.MeetingResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingResultController {

    private final MeetingResultService meetingResultService;

    /**
     * 모임 추천 만반 시간 조회
     * GET /meetings/{code}/time-recommendations
     *
     * @param code 모임 코드
     * @return 시간 추천 결과
     */
    @GetMapping("/{code}/time-recommendations")
    public ResponseEntity<TimeRecommendationResponse> getTimeRecommendation(
            @PathVariable String code) {
        TimeRecommendationResponse response = meetingResultService.getTimeRecommendation(code);
        return ResponseEntity.ok(response);
    }

    /**
     * 모임 추천 만반 장소 조회
     * GET /meetings/{code}/place-recommendations
     *
     * @param code 모임 코드
     * @return 장소 추천 결과
     */
    @GetMapping("/{code}/place-recommendations")
    public ResponseEntity<PlaceRecommendationResponse> getPlaceRecommendation(
            @PathVariable String code) {
        PlaceRecommendationResponse response = meetingResultService.getPlaceRecommendation(code);
        return ResponseEntity.ok(response);
    }
}
