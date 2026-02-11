package com.greedy.meetlink.meeting.service;

import com.greedy.meetlink.common.exception.MeetingCodeGenerationException;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.meeting.dto.response.MeetingDetailResponse;
import com.greedy.meetlink.meeting.dto.request.MeetingCreateRequest;
import com.greedy.meetlink.meeting.dto.request.MeetingUpdateRequest;
import com.greedy.meetlink.meeting.dto.response.MeetingResponse;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.meeting.util.MeetingCodeGenerator;
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
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 10;

    /**
     * 모임 생성
     *
     * @param request 모임 생성 요청 DTO (유효성 검증은 DTO 레벨에서 완료됨)
     * @return 생성된 모임 응답 DTO
     */
    @Transactional
    public MeetingResponse create(MeetingCreateRequest request) {

        String code = generateUniqueCode();

        Meeting meeting =
                Meeting.builder()
                        .name(request.getName())
                        .code(code)
                        .enableTimeRecommendation(request.getEnableTimeRecommendation())
                        .enablePlaceRecommendation(request.getEnablePlaceRecommendation())
                        .timeAvailabilityType(request.getTimeAvailabilityType())
                        .timeRangeStart(request.getTimeRangeStart())
                        .timeRangeEnd(request.getTimeRangeEnd())
                        .build();

        Meeting savedMeeting = meetingRepository.save(meeting);

        return MeetingResponse.from(savedMeeting);
    }

    /**
     * 모임 수정
     *
     * @param code 모임 code
     * @param request 모임 수정 요청 DTO
     * @return 수정된 모임 응답 DTO
     */
    @Transactional
    public MeetingResponse update(String code, MeetingUpdateRequest request) {
        Meeting meeting =
                meetingRepository
                        .findByCode(code)
                        .orElseThrow(() -> new MeetingNotFoundException(code));

        meeting.update(
                request.getName(),
                request.getEnableTimeRecommendation(),
                request.getEnablePlaceRecommendation(),
                request.getTimeAvailabilityType(),
                request.getTimeRangeStart(),
                request.getTimeRangeEnd());

        return MeetingResponse.from(meeting);
    }

    /**
     * 모임 삭제
     *
     * @param code 모임 code
     */
    @Transactional
    public void delete(String code) {
        Meeting meeting =
                meetingRepository
                        .findByCode(code)
                        .orElseThrow(() -> new MeetingNotFoundException(code));
        meetingRepository.deleteById(meeting.getId());
    }

    /**
     * 중복되지 않는 고유한 모임 코드 생성
     *
     * @return 생성된 코드
     */
    private String generateUniqueCode() {
        for (int i = 0; i < MAX_CODE_GENERATION_ATTEMPTS; i++) {
            String code = MeetingCodeGenerator.generateCode();
            if (!meetingRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new MeetingCodeGenerationException();
    }

    public MeetingDetailResponse getMeetingDetail(String code) {
        com.greedy.meetlink.meeting.entity.Meeting meeting = meetingRepository.findByCode(code)
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
