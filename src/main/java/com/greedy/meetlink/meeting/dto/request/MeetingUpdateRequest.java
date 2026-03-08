package com.greedy.meetlink.meeting.dto.request;

import com.greedy.meetlink.availability.entity.TimeAvailabilityType;
import com.greedy.meetlink.common.util.TimeRangeDeserializer;
import com.greedy.meetlink.meeting.validation.ValidTimeRange;
import com.greedy.meetlink.meeting.validation.ValidTimeRecommendation;
import com.greedy.meetlink.meeting.validation.provider.TimeRangeProvider;
import com.greedy.meetlink.meeting.validation.provider.TimeRecommendationProvider;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;

@Getter
@NoArgsConstructor
@ValidTimeRecommendation
@ValidTimeRange
public class MeetingUpdateRequest implements TimeRangeProvider, TimeRecommendationProvider {
    private String name;

    private Boolean enableTimeRecommendation;

    private Boolean enablePlaceRecommendation;

    private TimeAvailabilityType timeAvailabilityType;

    @JsonDeserialize(using = TimeRangeDeserializer.class)
    private LocalTime timeRangeStart;

    @JsonDeserialize(using = TimeRangeDeserializer.class)
    private LocalTime timeRangeEnd;
}
