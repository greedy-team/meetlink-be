package com.greedy.meetlink.result.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MeetingResultNotFoundException extends RuntimeException {
    private final String message;
    private final String resourceIdentifier;

    public static MeetingResultNotFoundException byMeetingId(Long meetingId) {
        return new MeetingResultNotFoundException(
                "해당 모임의 추천 결과를 찾을 수 없습니다.",
                String.valueOf(meetingId)
        );
    }

    public static MeetingResultNotFoundException byMeetingCode(String code) {
        return new MeetingResultNotFoundException(
                "해당 모임의 추천 결과를 찾을 수 없습니다.",
                code
        );
    }
}
