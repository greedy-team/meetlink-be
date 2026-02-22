package com.greedy.meetlink.availability.repository.projection;

import java.time.LocalDate;
import java.time.LocalTime;

public interface TimeAvailabilityHeatmapRow {
    LocalDate getDate();

    Integer getDayOfWeek();

    LocalTime getStartTime();

    Long getAvailableCount();
}
