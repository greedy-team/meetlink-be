package com.greedy.meetlink.meeting.dto.request;

import com.greedy.meetlink.availability.entity.TimeAvailabilityType;
import com.greedy.meetlink.common.util.TimeRangeDeserializer;
import com.greedy.meetlink.meeting.validation.ValidTimeRange;
import com.greedy.meetlink.meeting.validation.ValidTimeRecommendation;
import com.greedy.meetlink.meeting.validation.provider.TimeRangeProvider;
import com.greedy.meetlink.meeting.validation.provider.TimeRecommendationProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;

@Getter
@NoArgsConstructor
@ValidTimeRecommendation
@ValidTimeRange
public class MeetingCreateRequest implements TimeRangeProvider, TimeRecommendationProvider {
    @NotBlank(message = "모임 이름은 필수입니다.")
    private String name;

    @NotNull(message = "시간 추천 사용 여부는 필수입니다.")
    private Boolean enableTimeRecommendation;

    @NotNull(message = "장소 추천 사용 여부는 필수입니다.")
    private Boolean enablePlaceRecommendation;

    private TimeAvailabilityType timeAvailabilityType;

    @JsonDeserialize(using = TimeRangeDeserializer.class)
    private LocalTime timeRangeStart;

    @JsonDeserialize(using = TimeRangeDeserializer.class)
    private LocalTime timeRangeEnd;
}
