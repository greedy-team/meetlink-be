package com.greedy.meetlink.result.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.greedy.meetlink.candidate.dto.response.PlaceCandidateResponse;
import com.greedy.meetlink.candidate.dto.response.TimeCandidateResponse;
import com.greedy.meetlink.candidate.entity.CandidateCalculationStatus;
import com.greedy.meetlink.result.entity.MeetingResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MeetingResultResponse {
    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TimeCandidateResult {
        private final CandidateCalculationStatus calculationStatus;

        @JsonUnwrapped private final TimeCandidateResponse data;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PlaceCandidateResult {
        private final CandidateCalculationStatus calculationStatus;

        @JsonUnwrapped private final PlaceCandidateResponse data;
    }

    private final TimeCandidateResult timeCandidate;
    private final PlaceCandidateResult placeCandidate;

    public static MeetingResultResponse from(
            MeetingResult result,
            CandidateCalculationStatus placeStatus,
            CandidateCalculationStatus timeStatus) {
        return MeetingResultResponse.builder()
                .timeCandidate(
                        TimeCandidateResult.builder()
                                .calculationStatus(timeStatus)
                                .data(
                                        result.getTimeCandidate() != null
                                                ? TimeCandidateResponse.from(
                                                        result.getTimeCandidate())
                                                : null)
                                .build())
                .placeCandidate(
                        PlaceCandidateResult.builder()
                                .calculationStatus(placeStatus)
                                .data(
                                        result.getPlaceCandidate() != null
                                                ? PlaceCandidateResponse.from(
                                                        result.getPlaceCandidate())
                                                : null)
                                .build())
                .build();
    }
}
