package com.greedy.meetlink.common.validation;

import com.greedy.meetlink.common.exception.InvalidParticipantTokenException;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantValidator {
    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;

    public Participant validateAndGetParticipant(String meetingCode, String token) {
        Meeting meeting =
                meetingRepository
                        .findByCode(meetingCode)
                        .orElseThrow(MeetingNotFoundException::new);

        return participantRepository
                .findByMeetingAndToken(meeting, token)
                .orElseThrow(InvalidParticipantTokenException::new);
    }
}
