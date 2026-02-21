package com.greedy.meetlink.result.dto.response;

import com.greedy.meetlink.candidate.dto.response.PlaceCandidateResponse;
import com.greedy.meetlink.candidate.dto.response.TimeCandidateResponse;
import com.greedy.meetlink.result.entity.MeetingResult;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MeetingResultResponse {
    private final Long id;
    private final TimeCandidateResponse timeCandidate;
    private final PlaceCandidateResponse placeCandidate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static MeetingResultResponse from(MeetingResult result) {
        return MeetingResultResponse.builder()
                .id(result.getId())
                .timeCandidate(
                        result.getTimeCandidate() != null
                                ? TimeCandidateResponse.from(result.getTimeCandidate())
                                : null)
                .placeCandidate(
                        result.getPlaceCandidate() != null
                                ? PlaceCandidateResponse.from(result.getPlaceCandidate())
                                : null)
                .createdAt(result.getCreatedAt())
                .updatedAt(result.getUpdatedAt())
                .build();
    }
}
