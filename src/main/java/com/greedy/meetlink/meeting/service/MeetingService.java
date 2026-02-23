package com.greedy.meetlink.meeting.service;

import com.greedy.meetlink.availability.repository.TimeAvailabilityRepository;
import com.greedy.meetlink.candidate.repository.TimeCandidateRepository;
import com.greedy.meetlink.common.exception.MeetingCodeGenerationException;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.meeting.dto.request.MeetingCreateRequest;
import com.greedy.meetlink.meeting.dto.request.MeetingUpdateRequest;
import com.greedy.meetlink.meeting.dto.response.MeetingResponse;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.meeting.util.MeetingCodeGenerator;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 10;
    private final MeetingRepository meetingRepository;
    private final TimeAvailabilityRepository timeAvailabilityRepository;
    private final TimeCandidateRepository timeCandidateRepository;
    private final ParticipantRepository participantRepository;

    @Transactional(readOnly = true)
    public MeetingResponse get(String code) {
        Meeting meeting =
                meetingRepository.findByCode(code).orElseThrow(MeetingNotFoundException::new);

        return MeetingResponse.from(meeting);
    }

    @Transactional
    public MeetingResponse create(MeetingCreateRequest request) {
        String code = generateUniqueCode();

        Meeting meeting =
                Meeting.create(
                        request.getName(),
                        code,
                        request.getEnableTimeRecommendation(),
                        request.getEnablePlaceRecommendation(),
                        request.getTimeAvailabilityType(),
                        request.getTimeRangeStart(),
                        request.getTimeRangeEnd());

        Meeting savedMeeting = meetingRepository.save(meeting);

        return MeetingResponse.from(savedMeeting);
    }

    @Transactional
    public MeetingResponse update(String code, MeetingUpdateRequest request) {
        Meeting meeting =
                meetingRepository.findByCode(code).orElseThrow(MeetingNotFoundException::new);

        boolean timeRangeChanged = isTimeRangeChanged(meeting, request);

        meeting.update(
                request.getName(),
                request.getEnableTimeRecommendation(),
                request.getEnablePlaceRecommendation(),
                request.getTimeAvailabilityType(),
                request.getTimeRangeStart(),
                request.getTimeRangeEnd());

        if (timeRangeChanged) {
            if (meeting.getTimeRangeStart() != null) {
                timeAvailabilityRepository.deleteBeforeRangeStart(
                        meeting, meeting.getTimeRangeStart());
            }
            if (meeting.getTimeRangeEnd() != null) {
                timeAvailabilityRepository.deleteFromRangeEnd(meeting, meeting.getTimeRangeEnd());
            }
            participantRepository.resetTimeSubmittedIfNoAvailability(meeting);
            timeCandidateRepository.deleteByMeetingCode(code);
        }

        return MeetingResponse.from(meeting);
    }

    private boolean isTimeRangeChanged(Meeting meeting, MeetingUpdateRequest request) {
        LocalTime newStart = request.getTimeRangeStart();
        LocalTime newEnd = request.getTimeRangeEnd();
        return (newStart != null && !newStart.equals(meeting.getTimeRangeStart()))
                || (newEnd != null && !newEnd.equals(meeting.getTimeRangeEnd()));
    }

    @Transactional
    public void delete(String code) {
        Meeting meeting =
                meetingRepository.findByCode(code).orElseThrow(MeetingNotFoundException::new);
        meetingRepository.deleteById(meeting.getId());
    }

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_CODE_GENERATION_ATTEMPTS; i++) {
            String code = MeetingCodeGenerator.generateCode();
            if (!meetingRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new MeetingCodeGenerationException();
    }
}
