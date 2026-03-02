package com.greedy.meetlink.meeting.dto.response;

import com.greedy.meetlink.availability.entity.TimeAvailabilityType;
import com.greedy.meetlink.meeting.entity.Meeting;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MeetingResponse {
    private final String name;
    private final String code;
    private final boolean enableTimeRecommendation;
    private final boolean enablePlaceRecommendation;
    private final TimeAvailabilityType timeAvailabilityType;
    private final LocalTime timeRangeStart;
    private final LocalTime timeRangeEnd;

    public static MeetingResponse from(Meeting meeting) {
        return MeetingResponse.builder()
                .name(meeting.getName())
                .code(meeting.getCode())
                .enableTimeRecommendation(meeting.isEnableTimeRecommendation())
                .enablePlaceRecommendation(meeting.isEnablePlaceRecommendation())
                .timeAvailabilityType(meeting.getTimeAvailabilityType())
                .timeRangeStart(meeting.getTimeRangeStart())
                .timeRangeEnd(meeting.getTimeRangeEnd())
                .build();
    }
}
