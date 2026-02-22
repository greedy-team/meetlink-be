package com.greedy.meetlink.candidate.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TimeCandidatesResponse(
        List<Heatmap> heatmaps, List<TimeCandidateResponse> candidates) {
    // 히트맵 날짜별 정보
    public record Heatmap(LocalDate date, Integer dayOfWeek, List<TimeSlot> slots) {}

    // 히트맵 시간별 가용 인원
    public record TimeSlot(LocalTime startTime, Long availableCount) {}
}
