package com.greedy.meetlink.result.service;

import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.common.validation.ParticipantValidator;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.result.dto.response.MeetingResultResponse;
import com.greedy.meetlink.result.entity.MeetingResult;
import com.greedy.meetlink.result.repository.MeetingResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingResultService {
    private final MeetingResultRepository meetingResultRepository;
    private final MeetingRepository meetingRepository;
    private final ParticipantValidator participantValidator;

    public MeetingResultResponse getMeetingResult(String meetingCode, String token) {
        Meeting meeting =
                participantValidator.validateAndGetParticipant(meetingCode, token).getMeeting();

        MeetingResult result =
                meetingResultRepository
                        .findByMeeting(meeting)
                        .orElseThrow(MeetingNotFoundException::new);

        return MeetingResultResponse.from(result);
    }
}
