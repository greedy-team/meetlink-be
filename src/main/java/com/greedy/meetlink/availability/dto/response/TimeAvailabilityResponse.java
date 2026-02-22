package com.greedy.meetlink.availability.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeAvailabilityResponse {
    private final List<DailyHeatmap> heatmaps;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DailyHeatmap {
        private final LocalDate date;      // SPECIFIC_DATE용
        private final Integer dayOfWeek;   // WEEKLY용
        private final List<Slot> slots;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Slot {
        private final LocalTime startTime;
        private final int availableCount;
    }
}
