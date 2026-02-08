package com.greedy.meetlink.meeting.dto;

import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.entity.TimeAvailabilityType;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;

@Getter
public class MeetingResponse {
    
    private final Long id;
    private final String name;
    private final String code;
    private final boolean enableTimeRecommendation;
    private final boolean enablePlaceRecommendation;
    private final TimeAvailabilityType timeAvailabilityType;
    private final LocalTime timeRangeStart;
    private final LocalTime timeRangeEnd;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    public MeetingResponse(Meeting meeting) {
        this.id = meeting.getId();
        this.name = meeting.getName();
        this.code = meeting.getCode();
        this.enableTimeRecommendation = meeting.isEnableTimeRecommendation();
        this.enablePlaceRecommendation = meeting.isEnablePlaceRecommendation();
        this.timeAvailabilityType = meeting.getTimeAvailabilityType();
        this.timeRangeStart = meeting.getTimeRangeStart();
        this.timeRangeEnd = meeting.getTimeRangeEnd();
        this.createdAt = meeting.getCreatedAt();
        this.updatedAt = meeting.getUpdatedAt();
    }
    
    public static MeetingResponse from(Meeting meeting) {
        return new MeetingResponse(meeting);
    }
}
