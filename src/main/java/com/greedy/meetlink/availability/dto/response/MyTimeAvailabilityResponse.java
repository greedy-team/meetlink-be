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
public class MyTimeAvailabilityResponse {
    private final List<DailyAvailability> availabilities;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DailyAvailability {
        private final LocalDate date; // SPECIFIC_DATE
        private final Integer dayOfWeek; // WEEKLY
        private final List<LocalTime> startTimes;
    }
}
