package com.greedy.meetlink.availability.service;

import com.greedy.meetlink.availability.dto.request.TimeAvailabilityRequest;
import com.greedy.meetlink.availability.entity.TimeAvailability;
import com.greedy.meetlink.availability.entity.TimeAvailabilityType;
import com.greedy.meetlink.availability.repository.TimeAvailabilityRepository;
import com.greedy.meetlink.common.exception.InvalidTimeAvailabilityException;
import com.greedy.meetlink.common.validation.ParticipantValidator;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeAvailabilityService {
    private final TimeAvailabilityRepository timeAvailabilityRepository;
    private final ParticipantValidator participantValidator;

    @Transactional
    public void submit(String meetingCode, String token, TimeAvailabilityRequest request) {
        Participant participant =
                participantValidator.validateAndGetParticipant(meetingCode, token);
        Meeting meeting = participant.getMeeting();

        // 모임 타입 검증
        validateByMeetingType(meeting, request);

        // 기존 데이터 제거
        timeAvailabilityRepository.deleteByMeetingAndParticipant(meeting, participant);

        // 새 데이터 저장
        List<TimeAvailability> entities =
                request.getSlots().stream()
                        .distinct()
                        .map(
                                (slot) ->
                                        TimeAvailability.create(
                                                meeting,
                                                participant,
                                                slot.getDate(),
                                                slot.getDayOfWeek(),
                                                slot.getStartTime()))
                        .toList();

        timeAvailabilityRepository.saveAll(entities);

        // 시간 제출 여부 체크
        participant.markTimeSubmitted();
    }

    private void validateByMeetingType(Meeting meeting, TimeAvailabilityRequest request) {
        TimeAvailabilityType type = meeting.getTimeAvailabilityType();

        for (TimeAvailabilityRequest.TimeSlot slot : request.getSlots()) {
            switch (type) {
                case WEEKLY -> {
                    if (slot.getDayOfWeek() == null || slot.getDate() != null) {
                        throw new InvalidTimeAvailabilityException("WEEKLY 모임은 요일 기반 입력만 가능합니다.");
                    }
                }
                case SPECIFIC_DATE -> {
                    if (slot.getDate() == null || slot.getDayOfWeek() != null) {
                        throw new InvalidTimeAvailabilityException(
                                "SPECIFIC_DATE 모임은 날짜 기반 입력만 가능합니다.");
                    }
                }
            }
        }
    }
}
