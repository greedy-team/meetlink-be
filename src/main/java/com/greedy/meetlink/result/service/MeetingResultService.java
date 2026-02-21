package com.greedy.meetlink.result.service;

import com.greedy.meetlink.availability.TimeAvailability;
import com.greedy.meetlink.availability.TimeAvailabilityRepository;
import com.greedy.meetlink.candidate.TimeCandidate;
import com.greedy.meetlink.candidate.TimeCandidateRepository;
import com.greedy.meetlink.result.dto.PlaceRecommendationResponse;
import com.greedy.meetlink.result.dto.TimeRecommendationResponse;
import com.greedy.meetlink.result.entity.MeetingResult;
import com.greedy.meetlink.result.exception.MeetingResultNotFoundException;
import com.greedy.meetlink.result.mapper.TimeRecommendationMapper;
import com.greedy.meetlink.result.repository.MeetingResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingResultService {

    private final MeetingResultRepository meetingResultRepository;
    private final TimeAvailabilityRepository timeAvailabilityRepository;
    private final TimeCandidateRepository timeCandidateRepository;

    /**
     * 모임 코드로 시간 추천 정보 조회 (히트맵 + 추천 순위)
     * GET /meetings/{code}/time-recommendations
     *
     * [변경 이유]
     * 기존: TimeRecommendationResponse.of() 정적 메서드 직접 호출 (DTO에 변환 로직 혼재)
     * 변경: TimeRecommendationMapper.toResponse() 사용 → DTO는 순수 데이터 구조만 담당
     */
    public TimeRecommendationResponse getTimeRecommendation(String code) {
        List<TimeAvailability> availabilities = timeAvailabilityRepository.findByMeetingCode(code);
        List<TimeCandidate> candidates = timeCandidateRepository.findByMeetingCodeOrderByRank(code);

        return TimeRecommendationMapper.toResponse(availabilities, candidates);
    }

    public PlaceRecommendationResponse getPlaceRecommendation(String code) {
        MeetingResult meetingResult = meetingResultRepository.findWithPlaceByMeetingCode(code)
                .orElseThrow(() -> MeetingResultNotFoundException.byMeetingCode(code));

        return java.util.Optional.ofNullable(meetingResult.getPlaceCandidate())
                .map(PlaceRecommendationResponse::from)
                .orElse(PlaceRecommendationResponse.empty());
    }
}
