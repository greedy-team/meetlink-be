package com.greedy.meetlink.meeting.validation.provider;

import com.greedy.meetlink.availability.entity.TimeAvailabilityType;

public interface TimeRecommendationProvider extends TimeRangeProvider {
    Boolean getEnableTimeRecommendation();

    TimeAvailabilityType getTimeAvailabilityType();
}
