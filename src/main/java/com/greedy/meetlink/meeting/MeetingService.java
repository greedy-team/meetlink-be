package com.greedy.meetlink.meeting;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingService {
    private final MeetingRepository meetingRepository;

    public MeetingDetailResponse getMeetingDetail(String code) {
        Meeting meeting = meetingRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 모임입니다."));

        List<MeetingDetailResponse.ParticipantInfo> participantInfos = meeting.getParticipants().stream()
                .map(participant -> MeetingDetailResponse.ParticipantInfo.builder()
                        .id(participant.getId())
                        .nickname(participant.getNickname())
                        .hasEnteredPlace(participant.hasEnteredPlace())
                        .hasEnteredTime(participant.hasEnteredTime())
                        .build())
                .collect(Collectors.toList());

        return MeetingDetailResponse.builder()
                .id(meeting.getId())
                .name(meeting.getName())
                .code(meeting.getCode())
                .enableTimeRecommendation(meeting.isEnableTimeRecommendation())
                .enablePlaceRecommendation(meeting.isEnablePlaceRecommendation())
                .timeRangeStart(meeting.getTimeRangeStart())
                .timeRangeEnd(meeting.getTimeRangeEnd())
                .participants(participantInfos)
                .recommendationCompleted(meeting.isRecommendationCompleted())
                .build();
    }
}
