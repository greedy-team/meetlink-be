package com.greedy.meetlink.common.validation;

import java.time.LocalTime;

public interface TimeRangeProvider {
    LocalTime getTimeRangeStart();

    LocalTime getTimeRangeEnd();
}
