package com.greedy.meetlink.result.service;

import com.greedy.meetlink.candidate.entity.CandidateCalculationStatus;
import com.greedy.meetlink.candidate.service.PlaceCandidateService;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.common.validation.ParticipantValidator;
import com.greedy.meetlink.meeting.entity.Meeting;
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
    private final ParticipantValidator participantValidator;
    private final PlaceCandidateService placeCandidateService;

    public MeetingResultResponse getMeetingResult(String meetingCode, String token) {
        Meeting meeting =
                participantValidator.validateAndGetParticipant(meetingCode, token).getMeeting();

        MeetingResult result =
                meetingResultRepository
                        .findByMeeting(meeting)
                        .orElseThrow(MeetingNotFoundException::new);

        CandidateCalculationStatus placeStatus =
                placeCandidateService.isCalculating(meetingCode)
                        ? CandidateCalculationStatus.CALCULATING
                        : result.getPlaceCandidate() != null
                                ? CandidateCalculationStatus.DONE
                                : CandidateCalculationStatus.NONE;

        CandidateCalculationStatus timeStatus =
                result.getTimeCandidate() != null
                        ? CandidateCalculationStatus.DONE
                        : CandidateCalculationStatus.NONE;

        return MeetingResultResponse.from(result, placeStatus, timeStatus);
    }
}
