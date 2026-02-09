package com.greedy.meetlink.common.validation;

import com.greedy.meetlink.meeting.entity.TimeAvailabilityType;

public interface TimeRecommendationProvider extends TimeRangeProvider {
    Boolean getEnableTimeRecommendation();

    TimeAvailabilityType getTimeAvailabilityType();
}
