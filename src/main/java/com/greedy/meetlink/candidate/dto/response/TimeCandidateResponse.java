package com.greedy.meetlink.candidate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.greedy.meetlink.candidate.entity.TimeCandidate;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeCandidateResponse {
    private final LocalDate date;
    private final Integer dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Integer availableCount;
    private final Integer rank;

    public static TimeCandidateResponse from(TimeCandidate candidate) {
        return TimeCandidateResponse.builder()
                .date(candidate.getDate())
                .dayOfWeek(candidate.getDayOfWeek())
                .startTime(candidate.getStartTime())
                .endTime(candidate.getEndTime())
                .availableCount(candidate.getAvailableCount())
                .rank(candidate.getRank())
                .build();
    }
}
