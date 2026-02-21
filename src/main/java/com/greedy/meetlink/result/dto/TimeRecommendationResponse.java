package com.greedy.meetlink.result.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimeRecommendationResponse {

    /** 참여자 전체 히트맵 (날짜/요일별 가용 인원 현황) */
    private List<HeatmapResponse> heatmaps;

    /** 추천 순위 리스트 */
    private List<TimeCandidateResponse> candidates;

    @Getter
    @Builder
    public static class HeatmapResponse {
        private LocalDate date;
        private Integer dayOfWeek;
        private List<TimeSlotResponse> timeSlots;
    }

    @Getter
    @Builder
    public static class TimeSlotResponse {
        private LocalTime startTime;
        private Integer availableCount;
    }

    @Getter
    @Builder
    public static class TimeCandidateResponse {
        private Long id;
        private Integer rank;
        private LocalDate date;
        private Integer dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer availableCount;
    }
}
