package com.greedy.meetlink.candidate.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.candidate.repository.PlaceCandidateRepository;
import com.greedy.meetlink.candidate.repository.PlaceTravelInfoRepository;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import com.greedy.meetlink.place.client.PoiClient;
import com.greedy.meetlink.place.client.TransitClient;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse.PoiPlace;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlaceRecommendationServiceIntegrationTest {

    @Autowired private PlaceRecommendService placeRecommendService;

    @Autowired private MeetingRepository meetingRepository;
    @Autowired private ParticipantRepository participantRepository;
    @Autowired private LocationAvailabilityRepository locationAvailabilityRepository;
    @Autowired private PlaceCandidateRepository placeCandidateRepository;
    @Autowired private PlaceTravelInfoRepository placeTravelInfoRepository;

    @MockitoBean private TransitClient transitClient;
    @MockitoBean private PoiClient poiClient;

    @Test
    @DisplayName("[통합] Mock TMap API를 사용하여 장소 추천 로직 검증")
    void recommend_mockApi() {
        // given
        Meeting meeting =
                Meeting.create(
                        "Mock API Test Meeting",
                        "MOCK-TEST-CODE-001",
                        true,
                        true,
                        null,
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0));
        meetingRepository.save(meeting);

        createParticipantWithLocation(meeting, "UserA", "token1", "서울시청", 37.5665, 126.9780);
        createParticipantWithLocation(meeting, "UserB", "token2", "강남역", 37.4979, 127.0276);
        createParticipantWithLocation(meeting, "UserC", "token3", "홍대입구", 37.5574, 126.9240);

        BDDMockito.given(
                        transitClient.getTravelTimeMinutes(
                                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .willReturn(30.0);
        BDDMockito.given(poiClient.searchNearby(ArgumentMatchers.any()))
                .willReturn(List.of(new PoiPlace("Mock Cafe", "Seoul Mock Street", 37.55, 126.99)));

        // when
        placeRecommendService.recommendAndSave("MOCK-TEST-CODE-001");

        // then
        List<PlaceCandidate> candidates = placeCandidateRepository.findByMeeting(meeting);
        assertThat(candidates).isNotEmpty();
        assertThat(candidates.get(0).getName()).isEqualTo("Mock Cafe");
        assertThat(candidates.get(0).getAvgTravelTime()).isEqualTo(30.0);
    }

    private void createParticipantWithLocation(
            Meeting meeting,
            String nickname,
            String token,
            String address,
            double lat,
            double lon) {
        Participant participant = Participant.create(meeting, nickname, token);
        participantRepository.save(participant);
        locationAvailabilityRepository.save(
                LocationAvailability.create(participant, address, lat, lon));
    }
}