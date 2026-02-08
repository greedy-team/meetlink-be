package com.greedy.meetlink.meeting.dto;

import com.greedy.meetlink.meeting.entity.TimeAvailabilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MeetingCreateRequest {

    @NotBlank(message = "모임 이름은 필수입니다.")
    private String name;

    @NotNull(message = "시간 추천 사용 여부는 필수입니다.")
    private Boolean enableTimeRecommendation;

    @NotNull(message = "장소 추천 사용 여부는 필수입니다.")
    private Boolean enablePlaceRecommendation;

    private TimeAvailabilityType timeAvailabilityType;

    private LocalTime timeRangeStart;

    private LocalTime timeRangeEnd;

    public MeetingCreateRequest(String name, Boolean enableTimeRecommendation,
                                Boolean enablePlaceRecommendation,
                                TimeAvailabilityType timeAvailabilityType,
                                LocalTime timeRangeStart, LocalTime timeRangeEnd) {
        this.name = name;
        this.enableTimeRecommendation = enableTimeRecommendation;
        this.enablePlaceRecommendation = enablePlaceRecommendation;
        this.timeAvailabilityType = timeAvailabilityType;
        this.timeRangeStart = timeRangeStart;
        this.timeRangeEnd = timeRangeEnd;
    }
}
