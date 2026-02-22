package com.greedy.meetlink.place.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.candidate.PlaceCandidateRepository;
import com.greedy.meetlink.candidate.entity.PlaceCandidate;
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
@ActiveProfiles("test") // test 프로파일 -> FakeTransitClient, FakePoiClient 사용
@Transactional
class PlaceRecommendationServiceFakeApiTest {

    @Autowired private PlaceRecommendService placeRecommendationService;
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

        // 서울시청, 강남역, 홍대입구
        createParticipant(meeting, "UserA", "token1", 37.5665, 126.9780);
        createParticipant(meeting, "UserB", "token2", 37.4979, 127.0276);
        createParticipant(meeting, "UserC", "token3", 37.5574, 126.9240);

        // when
        System.out.println(">>> [START] Fake Client 기반 추천 로직 실행 <<<");
        placeRecommendationService.recommendAndSave(meeting);
        System.out.println(">>> [END] 추천 로직 완료 <<<");

        // then
        List<PlaceCandidate> candidates = placeCandidateRepository.findByMeeting(meeting);
        assertThat(candidates).isNotEmpty();

        PlaceCandidate first = candidates.get(0);
        System.out.println("\n>>> 추천 결과: " + first.getName() + " (" + first.getAddress() + ")");
        System.out.println(">>> 평균 이동 시간: " + first.getAvgTravelTime() + "분 (Fake 계산 결과)");

        assertThat(first.getAvgTravelTime()).isGreaterThan(0.0);
        // FakePoiClient가 반환하는 이름 패턴 확인
        assertThat(first.getName()).startsWith("Fake Cafe at");
    }

    private void createParticipant(Meeting m, String nick, String token, double lat, double lon) {
        Participant p = Participant.create(m, nick, token);
        participantRepository.save(p);
        locationAvailabilityRepository.save(LocationAvailability.create(p, "Addr", lat, lon));
    }
}
