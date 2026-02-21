package com.greedy.meetlink.candidate.dto.response;

import com.greedy.meetlink.candidate.TimeCandidate;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeCandidateResponse {
    private final Long id;
    private final LocalDate date;
    private final Integer dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Integer availableCount;
    private final Integer rank;

    public static TimeCandidateResponse from(TimeCandidate candidate) {
        return TimeCandidateResponse.builder()
                .id(candidate.getId())
                .date(candidate.getDate())
                .dayOfWeek(candidate.getDayOfWeek())
                .startTime(candidate.getStartTime())
                .endTime(candidate.getEndTime())
                .availableCount(candidate.getAvailableCount())
                .rank(candidate.getRank())
                .build();
    }
}
