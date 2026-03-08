package com.greedy.meetlink.participant.service;

import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.availability.repository.TimeAvailabilityRepository;
import com.greedy.meetlink.candidate.event.ParticipantLeftEvent;
import com.greedy.meetlink.common.exception.DuplicateNicknameException;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.common.validation.ParticipantValidator;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.dto.request.ParticipantJoinRequest;
import com.greedy.meetlink.participant.dto.response.ParticipantJoinResponse;
import com.greedy.meetlink.participant.dto.response.ParticipantResponse;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantService {
    private final ParticipantRepository participantRepository;
    private final MeetingRepository meetingRepository;
    private final TimeAvailabilityRepository timeAvailabilityRepository;
    private final LocationAvailabilityRepository locationAvailabilityRepository;
    private final ParticipantValidator participantValidator;
    private final ApplicationEventPublisher eventPublisher;

    // 모임 참여
    @Transactional
    public ParticipantJoinResponse join(String meetingCode, ParticipantJoinRequest request) {
        Meeting meeting =
                meetingRepository
                        .findByCode(meetingCode)
                        .orElseThrow(MeetingNotFoundException::new);

        if (participantRepository.existsByMeetingAndNickname(meeting, request.getNickname())) {
            throw new DuplicateNicknameException();
        }

        String generatedToken = java.util.UUID.randomUUID().toString();

        Participant participant =
                Participant.create(meeting, request.getNickname(), generatedToken);

        participantRepository.save(participant);

        return ParticipantJoinResponse.from(generatedToken);
    }

    // 참여자 목록 조회
    public List<ParticipantResponse> getParticipants(String meetingCode, String token) {
        if (token == null) {
            Meeting meeting =
                    meetingRepository
                            .findByCode(meetingCode)
                            .orElseThrow(MeetingNotFoundException::new);
            return participantRepository
                    .findByMeetingOrderByCreatedAtAscNicknameAsc(meeting)
                    .stream()
                    .map(ParticipantResponse::of)
                    .toList();
        }

        Participant participant =
                participantValidator.validateAndGetParticipant(meetingCode, token);
        Meeting meeting = participant.getMeeting();

        boolean timeEnabled = meeting.isEnableTimeRecommendation();
        boolean placeEnabled = meeting.isEnablePlaceRecommendation();

        List<Participant> participants =
                participantRepository.findByMeetingOrderByCreatedAtAscNicknameAsc(meeting);

        Set<Long> timeSubmittedIds =
                new HashSet<>(timeAvailabilityRepository.findSubmittedParticipantIds(meeting));
        Set<Long> locationSubmittedIds =
                new HashSet<>(locationAvailabilityRepository.findSubmittedParticipantIds(meeting));

        return participants.stream()
                .map(
                        (p) ->
                                ParticipantResponse.of(
                                        p,
                                        !timeEnabled || timeSubmittedIds.contains(p.getId()),
                                        !placeEnabled || locationSubmittedIds.contains(p.getId())))
                .toList();
    }

    // 내 상태 조회
    public ParticipantResponse status(String meetingCode, String token) {
        Participant participant =
                participantValidator.validateAndGetParticipant(meetingCode, token);
        Meeting meeting = participant.getMeeting();

        boolean isTimeSubmitted =
                !meeting.isEnableTimeRecommendation()
                        || timeAvailabilityRepository.existsByMeetingAndParticipant(
                                meeting, participant);
        boolean isPlaceSubmitted =
                !meeting.isEnablePlaceRecommendation()
                        || locationAvailabilityRepository.existsByParticipant(participant);

        return ParticipantResponse.of(participant, isTimeSubmitted, isPlaceSubmitted);
    }

    // 모임 나가기
    @Transactional
    public void leave(String meetingCode, String token) {
        Participant participant =
                participantValidator.validateAndGetParticipant(meetingCode, token);
        participantRepository.delete(participant);
        eventPublisher.publishEvent(new ParticipantLeftEvent(meetingCode));
    }
}
