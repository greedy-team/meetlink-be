package com.greedy.meetlink.participant.service;

import com.greedy.meetlink.common.exception.DuplicateNicknameException;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.common.exception.ParticipantNotFoundException;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.dto.request.ParticipantJoinRequest;
import com.greedy.meetlink.participant.dto.response.ParticipantInfoResponse;
import com.greedy.meetlink.participant.dto.response.ParticipantJoinResponse;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final MeetingRepository meetingRepository;

    // 모임 참여
    @Transactional
    public ParticipantJoinResponse join(String meetingCode, ParticipantJoinRequest request) {
        Meeting meeting =
                meetingRepository
                        .findByCode(meetingCode)
                        .orElseThrow(() -> new MeetingNotFoundException(meetingCode));

        if (participantRepository.existsByMeetingAndNickname(meeting, request.getNickname())) {
            throw new DuplicateNicknameException();
        }

        Participant participant =
                Participant.create(meeting, request.getNickname(), request.getToken());

        participantRepository.save(participant);

        return new ParticipantJoinResponse(true);
    }

    // 참여자 목록 조회
    public List<ParticipantInfoResponse> getParticipants(String meetingCode) {
        Meeting meeting =
                meetingRepository
                        .findByCode(meetingCode)
                        .orElseThrow(() -> new MeetingNotFoundException(meetingCode));

        return participantRepository.findWithDetailsByMeeting(meeting).stream()
                .map(ParticipantInfoResponse::from)
                .collect(Collectors.toList());
    }

    // 내 상태 조회
    public ParticipantInfoResponse getMyStatus(String meetingCode, String token) {
        Participant participant = findParticipantByToken(meetingCode, token);
        return ParticipantInfoResponse.from(participant);
    }

    // 모임 나가기
    @Transactional
    public void leave(String meetingCode, String token) {
        Participant participant = findParticipantByToken(meetingCode, token);
        participantRepository.delete(participant);
    }

    // 토큰으로 참여자 찾기 검증 로직
    private Participant findParticipantByToken(String meetingCode, String token) {
        return participantRepository
                .findByMeeting_CodeAndToken(meetingCode, token)
                .orElseThrow(() -> new ParticipantNotFoundException());
    }
}
