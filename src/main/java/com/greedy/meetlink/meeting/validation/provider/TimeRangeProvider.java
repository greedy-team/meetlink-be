package com.greedy.meetlink.meeting.validation.provider;

import java.time.LocalTime;

public interface TimeRangeProvider {
    LocalTime getTimeRangeStart();

    LocalTime getTimeRangeEnd();
}
