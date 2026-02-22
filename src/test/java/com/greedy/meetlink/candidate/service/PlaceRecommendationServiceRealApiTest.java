package com.greedy.meetlink.candidate.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.candidate.repository.PlaceCandidateRepository;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlaceRecommendationServiceRealApiTest {

    @Autowired private PlaceRecommendService placeRecommendService;

    @Autowired private MeetingRepository meetingRepository;
    @Autowired private ParticipantRepository participantRepository;
    @Autowired private LocationAvailabilityRepository locationAvailabilityRepository;
    @Autowired private PlaceCandidateRepository placeCandidateRepository;

    @Test
    @DisplayName("[REAL-API] 실제 TMap API 연동 - 서울 3지점(시청/강남/홍대) 추천")
    void recommend_withRealApi() {
        // given
        Meeting meeting =
                Meeting.create(
                        "Real API Test Meeting",
                        "REAL-API-CODE-001",
                        true,
                        true,
                        null,
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0));
        meetingRepository.save(meeting);

        createParticipantWithLocation(meeting, "UserA", "token1", "서울시청", 37.5665, 126.9780);
        createParticipantWithLocation(meeting, "UserB", "token2", "강남역", 37.4979, 127.0276);
        createParticipantWithLocation(meeting, "UserC", "token3", "홍대입구", 37.5574, 126.9240);

        // when
        System.out.println(">>> [START] 실제 TMap API 호출 <<<");
        placeRecommendService.recommendAndSave("REAL-API-CODE-001");
        System.out.println(">>> [END] 추천 로직 완료 <<<");

        // then
        List<PlaceCandidate> candidates = placeCandidateRepository.findByMeeting(meeting);
        assertThat(candidates).isNotEmpty();
        assertThat(candidates.get(0).getName()).isNotNull();
        assertThat(candidates.get(0).getAvgTravelTime()).isGreaterThan(0.0);
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
