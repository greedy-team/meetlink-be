package com.greedy.meetlink.place.service;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.candidate.PlaceCandidate;
import com.greedy.meetlink.candidate.PlaceCandidateRepository;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import com.greedy.meetlink.place.client.TMapPoiClient;
import com.greedy.meetlink.place.client.TMapTransitClient;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse.PoiPlace;
import com.greedy.meetlink.place.repository.PlaceTravelInfoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlaceRecommendationServiceIntegrationTest {

    @Autowired
    private PlaceRecommendService placeRecommendationService;

    @Autowired private MeetingRepository meetingRepository;
    @Autowired private ParticipantRepository participantRepository;
    @Autowired private LocationAvailabilityRepository locationAvailabilityRepository;
    @Autowired private PlaceCandidateRepository placeCandidateRepository;
    @Autowired private PlaceTravelInfoRepository placeTravelInfoRepository;

    // 실제 API 대신 가짜 빈(MockBean) 사용 -> 쿼터 소진 문제 해결
    @MockitoBean private TMapTransitClient tMapTransitClient;
    @MockitoBean private TMapPoiClient tMapPoiClient;

    @Test
    @DisplayName("[통합] Mock TMap API를 사용하여 장소 추천 로직 검증")
    void recommend_mockApi() {
        // given
        // 1. 모임 생성
        Meeting meeting = Meeting.create(
                "Mock API Test Meeting",
                "MOCK-TEST-CODE-001",
                true,
                true,
                null,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        );
        meetingRepository.save(meeting);

        // 2. 참여자 3명 생성 (서울 시내)
        createParticipantWithLocation(meeting, "UserA", "token1", "서울시청", 37.5665, 126.9780);
        createParticipantWithLocation(meeting, "UserB", "token2", "강남역", 37.4979, 127.0276);
        createParticipantWithLocation(meeting, "UserC", "token3", "홍대입구", 37.5574, 126.9240);

        // 3. Mock 설정 (가짜 응답)
        // 어떤 좌표 요청이 오든 30분 소요된다고 가정
        BDDMockito.given(tMapTransitClient.getTravelTimeMinutes(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .willReturn(30.0);

        // 어떤 좌표 검색이든 "Mock Cafe"가 있다고 가정
        BDDMockito.given(tMapPoiClient.searchNearby(ArgumentMatchers.any()))
                .willReturn(List.of(
                        new PoiPlace("Mock Cafe", "Seoul Mock Street", 37.55, 126.99)
                ));

        // when
        System.out.println(">>> [START] Mock API 기반 추천 로직 실행 <<<");
        placeRecommendationService.recommendAndSave(meeting);
        System.out.println(">>> [END] 추천 로직 완료 <<<");

        // then
        List<PlaceCandidate> candidates = placeCandidateRepository.findByMeeting(meeting);
        
        System.out.println("\n>>> [RESULT] 추천된 장소 목록 (" + candidates.size() + "개) <<<");
        candidates.forEach(c -> {
            System.out.printf("- %s (%s) | 평균이동시간: %.1f분 | 순위: %d%n",
                    c.getName(), c.getAddress(), c.getAvgTravelTime(), c.getRank());
        });

        assertThat(candidates).isNotEmpty();
        assertThat(candidates.get(0).getName()).isEqualTo("Mock Cafe");
        assertThat(candidates.get(0).getAvgTravelTime()).isEqualTo(30.0);
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
