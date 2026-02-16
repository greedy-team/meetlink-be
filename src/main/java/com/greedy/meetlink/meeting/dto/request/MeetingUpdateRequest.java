package com.greedy.meetlink.meeting.dto.request;

import com.greedy.meetlink.common.validation.TimeRangeProvider;
import com.greedy.meetlink.common.validation.TimeRecommendationProvider;
import com.greedy.meetlink.meeting.entity.TimeAvailabilityType;
import com.greedy.meetlink.meeting.validation.ValidTimeRange;
import com.greedy.meetlink.meeting.validation.ValidTimeRecommendation;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@ValidTimeRecommendation
@ValidTimeRange
public class MeetingUpdateRequest implements TimeRangeProvider, TimeRecommendationProvider {

    private String name;

    private Boolean enableTimeRecommendation;

    private Boolean enablePlaceRecommendation;

    private TimeAvailabilityType timeAvailabilityType;

    private LocalTime timeRangeStart;

    private LocalTime timeRangeEnd;
}
