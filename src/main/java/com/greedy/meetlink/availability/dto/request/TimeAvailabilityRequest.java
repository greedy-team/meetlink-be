package com.greedy.meetlink.availability.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TimeAvailabilityRequest {
    @NotEmpty(message = "시간 선택은 최소 1개 이상이어야 합니다.")
    private List<Availability> availabilities;

    @Getter
    public static class Availability {
        private Integer dayOfWeek; // WEEKLY 모임 전용
        private LocalDate date; // SPECIFIC_DATE 모임 전용

        @NotEmpty(message = "선택된 시간은 최소 1개 이상이어야 합니다.")
        private List<@NotNull(message = "시작 시간은 필수입니다.") LocalTime> startTimes;
    }
}
