package com.greedy.meetlink.meeting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MeetingDetailResponse {
    private Long id;
    private String name;
    private String code;
    private boolean enableTimeRecommendation;
    private boolean enablePlaceRecommendation;
    private LocalTime timeRangeStart;
    private LocalTime timeRangeEnd;

    private List<ParticipantInfo> participants;

    private boolean recommendationCompleted;

    @Getter
    @Builder
    public static class ParticipantInfo {
        private Long id;
        private String nickname;
        private boolean hasEnteredPlace;
        private boolean hasEnteredTime;
    }
}
