package com.greedy.meetlink.availability.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TimeAvailabilityRequest {
    @NotEmpty(message = "시간 선택은 최소 1개 이상이어야 합니다.")
    private List<TimeSlot> slots;

    @Getter
    public static class TimeSlot {
        private Integer dayOfWeek; // WEEKLY 모임 전용
        private LocalDate date; // SPECIFIC_DATE 모임 전용

        @NotNull(message = "시작 시간은 필수입니다.")
        private LocalTime startTime;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TimeSlot other)) return false;
            return Objects.equals(date, other.date)
                    && Objects.equals(dayOfWeek, other.dayOfWeek)
                    && Objects.equals(startTime, other.startTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(date, dayOfWeek, startTime);
        }
    }
}
