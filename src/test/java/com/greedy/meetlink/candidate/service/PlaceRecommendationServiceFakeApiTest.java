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
class PlaceRecommendationServiceFakeApiTest {

    @Autowired private PlaceCandidateService placeCandidateService;
    @Autowired private MeetingRepository meetingRepository;
    @Autowired private ParticipantRepository participantRepository;
    @Autowired private LocationAvailabilityRepository locationAvailabilityRepository;
    @Autowired private PlaceCandidateRepository placeCandidateRepository;

    @Test
    @DisplayName("[FAKE-API] Fake Client(Transit/POI)를 사용하여 장소 추천 로직 통합 검증")
    void recommend_withFakeApi() {
        // given
        Meeting meeting =
                Meeting.create(
                        "Fake API Test",
                        "FAKE-CODE-002",
                        true,
                        true,
                        null,
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0));
        meetingRepository.save(meeting);

        createParticipant(meeting, "UserA", "token1", 37.5665, 126.9780);
        createParticipant(meeting, "UserB", "token2", 37.4979, 127.0276);
        createParticipant(meeting, "UserC", "token3", 37.5574, 126.9240);

        // when
        placeCandidateService.calculate("FAKE-CODE-002");

        // then
        List<PlaceCandidate> candidates = placeCandidateRepository.findByMeeting(meeting);
        assertThat(candidates).isNotEmpty();

        PlaceCandidate first = candidates.get(0);
        assertThat(first.getAvgTravelTime()).isGreaterThan(0.0);
        assertThat(first.getName()).startsWith("Fake Cafe at");
    }

    private void createParticipant(Meeting m, String nick, String token, double lat, double lon) {
        Participant p = Participant.create(m, nick, token);
        participantRepository.save(p);
        locationAvailabilityRepository.save(LocationAvailability.create(p, "Addr", lat, lon));
    }
}
