package com.greedy.meetlink.meeting.service;

import com.greedy.meetlink.meeting.dto.MeetingCreateRequest;
import com.greedy.meetlink.meeting.dto.MeetingResponse;
import com.greedy.meetlink.meeting.dto.MeetingUpdateRequest;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.exception.MeetingNotFoundException;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.meeting.util.MeetingCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 10;

    /**
     * 모임 생성
     * @param request 모임 생성 요청 DTO (유효성 검증은 DTO 레벨에서 완료됨)
     * @return 생성된 모임 응답 DTO
     */
    @Transactional
    public MeetingResponse createMeeting(MeetingCreateRequest request) {

        String code = generateUniqueCode();

        Meeting meeting = Meeting.builder()
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
     * @param id 모임 ID
     * @param request 모임 수정 요청 DTO (유효성 검증은 DTO 레벨에서 완료됨)
     * @return 수정된 모임 응답 DTO
     */
    @Transactional
    public MeetingResponse updateMeeting(Long id, MeetingUpdateRequest request) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new MeetingNotFoundException(id));

        meeting.update(
                request.getName(),
                request.getEnableTimeRecommendation(),
                request.getEnablePlaceRecommendation(),
                request.getTimeAvailabilityType(),
                request.getTimeRangeStart(),
                request.getTimeRangeEnd()
        );

        return MeetingResponse.from(meeting);
    }

    /**
     * 모임 ID로 조회
     * @param id 모임 ID
     * @return 모임 응답 DTO
     */
    public MeetingResponse getMeetingById(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new MeetingNotFoundException(id));

        return MeetingResponse.from(meeting);
    }

    /**
     * 모임 삭제
     * @param id 모임 ID
     */
    @Transactional
    public void deleteMeeting(Long id) {
        if (!meetingRepository.existsById(id)) {
            throw new MeetingNotFoundException(id);
        }
        meetingRepository.deleteById(id);
    }

    /**
     * 중복되지 않는 고유한 모임 코드 생성
     * @return 생성된 코드
     */
    private String generateUniqueCode() {
        for (int i = 0; i < MAX_CODE_GENERATION_ATTEMPTS; i++) {
            String code = MeetingCodeGenerator.generateCode();
            if (!meetingRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("고유한 모임 코드 생성에 실패했습니다.");
    }
}
