package com.greedy.meetlink.result.service;

import com.greedy.meetlink.availability.TimeAvailability;
import com.greedy.meetlink.availability.TimeAvailabilityRepository;
import com.greedy.meetlink.candidate.TimeCandidate;
import com.greedy.meetlink.candidate.TimeCandidateRepository;
import com.greedy.meetlink.result.entity.MeetingResult;
import com.greedy.meetlink.result.repository.MeetingResultRepository;
import com.greedy.meetlink.result.dto.PlaceRecommendationResponse;
import com.greedy.meetlink.result.dto.TimeRecommendationResponse;
import com.greedy.meetlink.result.exception.MeetingResultNotFoundException;
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
     * GET /api/v1/meetings/{code}/time-recommendations
     *
     * @param code 모임 코드
     * @return 시간 추천 응답 (히트맵 + 후보군)
     */
    public TimeRecommendationResponse getTimeRecommendation(String code) {
        // 1. 히트맵 데이터 조회 (모든 참여자의 TimeAvailability)
        List<TimeAvailability> availabilities = timeAvailabilityRepository.findByMeetingCode(code);

        // 2. 추천 후보 조회 (계산된 TimeCandidate 리스트)
        List<TimeCandidate> candidates = timeCandidateRepository.findByMeetingCodeOrderByRank(code);

        // 3. 응답 생성
        return TimeRecommendationResponse.of(availabilities, candidates);
    }
    /**
     * 모임 코드로 장소 추천 결과 조회
     * GET /meetings/{code}/place-recommendations
     *
     * @param code 모임 코드
     * @return 장소 추천 결과
     */
    public PlaceRecommendationResponse getPlaceRecommendation(String code) {
        MeetingResult meetingResult = meetingResultRepository.findByMeetingCode(code)
                .orElseThrow(() -> MeetingResultNotFoundException.byMeetingCode(code));

        if (meetingResult.getPlaceCandidate() == null) {
            throw new IllegalStateException("해당 모임의 장소 추천 결과가 없습니다. code: " + code);
        }

        return PlaceRecommendationResponse.from(meetingResult.getPlaceCandidate());
    }
}
