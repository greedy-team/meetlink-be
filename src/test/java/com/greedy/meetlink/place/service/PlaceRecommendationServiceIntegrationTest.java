package com.greedy.meetlink.place.service;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.candidate.PlaceCandidate;
import com.greedy.meetlink.candidate.PlaceCandidateRepository;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import com.greedy.meetlink.result.PlaceTravelInfoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlaceRecommendationServiceIntegrationTest {

    @Autowired
    private PlaceRecommendationService placeRecommendationService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private LocationAvailabilityRepository locationAvailabilityRepository;

    @Autowired
    private PlaceCandidateRepository placeCandidateRepository;

    @Autowired
    private PlaceTravelInfoRepository placeTravelInfoRepository;

    @Test
    @DisplayName("[통합] 실제 TMap API 호출하여 서울 시내 3명 장소 추천 검증")
    void recommend_realApiCall() {
        // given
        // 1. 모임 생성
        Meeting meeting = Meeting.create(
                "Real API Test Meeting",
                "REAL-TEST-CODE-001",
                true,
                true,
                null,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        );
        meetingRepository.save(meeting);

        // 2. 참여자 3명 생성 및 위치 설정 (서울 시내)
        // A: 서울시청 (37.5665, 126.9780)
        createParticipantWithLocation(meeting, "UserA", "token1", "서울시청", 37.5665, 126.9780);

        // B: 강남역 (37.4979, 127.0276)
        createParticipantWithLocation(meeting, "UserB", "token2", "강남역", 37.4979, 127.0276);

        // C: 홍대입구 (37.5574, 126.9240)
        createParticipantWithLocation(meeting, "UserC", "token3", "홍대입구", 37.5574, 126.9240);

        // when
        System.out.println(">>> [START] 실제 TMap API 호출 (시간 소요 가능) <<<");
        placeRecommendationService.recommend(meeting.getCode());
        System.out.println(">>> [END] 추천 로직 완료 <<<");

        // then
        // 결과가 DB에 저장되었는지 확인
        List<PlaceCandidate> candidates = placeCandidateRepository.findByMeeting(meeting);
        
        System.out.println(" >>> [RESULT] 추천된 장소 목록 (" + candidates.size() + "개) <<<");
        candidates.forEach(c -> {
            System.out.printf("- %s (%s) | 평균이동시간: %.1f분 | 순위: %d%n",
                    c.getName(), c.getAddress(), c.getAvgTravelTime(), c.getRank());
        });

        assertThat(candidates).isNotEmpty();
        
        // 최소 1개 이상의 후보가 있어야 하며, 이동 시간이 0보다 커야 함
        PlaceCandidate first = candidates.get(0);
        assertThat(first.getName()).isNotNull();
        assertThat(first.getAvgTravelTime()).isGreaterThan(0);
    }

    private void createParticipantWithLocation(
            Meeting meeting, String nickname, String token, 
            String address, double lat, double lon) {
        
        Participant participant = Participant.create(meeting, nickname, token);
        participantRepository.save(participant);

        LocationAvailability location = LocationAvailability.create(participant, address, lat, lon);
        locationAvailabilityRepository.save(location);
    }
}
