package com.greedy.meetlink.meeting.dto.request;

import com.greedy.meetlink.meeting.entity.TimeAvailabilityType;
import com.greedy.meetlink.meeting.validation.ValidTimeRange;
import com.greedy.meetlink.meeting.validation.ValidTimeRecommendation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@ValidTimeRecommendation
@ValidTimeRange
public class MeetingUpdateRequest{
    
    @NotBlank(message = "모임 이름은 필수입니다.")
    private String name;
    
    @NotNull(message = "시간 추천 사용 여부는 필수입니다.")
    private Boolean enableTimeRecommendation;
    
    @NotNull(message = "장소 추천 사용 여부는 필수입니다.")
    private Boolean enablePlaceRecommendation;
    
    private TimeAvailabilityType timeAvailabilityType;
    
    private LocalTime timeRangeStart;
    
    private LocalTime timeRangeEnd;
}
