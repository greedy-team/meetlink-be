package com.greedy.meetlink.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.candidate.PlaceCandidateRepository;
import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import com.greedy.meetlink.place.algorithm.*;
import com.greedy.meetlink.place.client.PoiClient;
import com.greedy.meetlink.place.client.TransitClient;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse.PoiPlace;
import com.greedy.meetlink.place.repository.PlaceTravelInfoRepository;
import com.greedy.meetlink.result.entity.MeetingResult;
import com.greedy.meetlink.result.repository.MeetingResultRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PlaceRecommendServiceTest {

    @Mock private PlaceCandidateRepository placeCandidateRepository;
    @Mock private PlaceTravelInfoRepository placeTravelInfoRepository;
    @Mock private MeetingResultRepository meetingResultRepository;
    @Mock private ParticipantRepository participantRepository;
    @Mock private LocationAvailabilityRepository locationAvailabilityRepository;

    @Mock private TransitClient transitClient;
    @Mock private PoiClient poiClient;

    private PlaceRecommendService placeRecommendService;

    @BeforeEach
    void setUp() {
        GeometricMedianCalculator geometricMedianCalculator = new GeometricMedianCalculator();
        PolarSamplingGenerator polarSamplingGenerator = new PolarSamplingGenerator();

        CandidateFilter candidateFilter = new CandidateFilter(transitClient);
        ReflectionTestUtils.setField(candidateFilter, "tmapCallDelayMs", 0L);

        ScoreCalculator scoreCalculator = new ScoreCalculator();
        CandidateScorer candidateScorer = new CandidateScorer(scoreCalculator);
        PlaceMapper placeMapper = new PlaceMapper(poiClient);

        placeRecommendService =
                new PlaceRecommendService(
                        geometricMedianCalculator,
                        polarSamplingGenerator,
                        candidateFilter,
                        candidateScorer,
                        placeMapper,
                        placeCandidateRepository,
                        placeTravelInfoRepository,
                        meetingResultRepository,
                        participantRepository,
                        locationAvailabilityRepository);
    }

    @Test
    @DisplayName("장소 추천 및 저장 전체 프로세스 검증")
    void recommendAndSave_Success() {
        // given
        Meeting meeting = createMeeting(1L, "TEST-CODE");
        Participant p1 = createParticipant(1L, meeting, "UserA");
        Participant p2 = createParticipant(2L, meeting, "UserB");
        List<Participant> participants = List.of(p1, p2);

        LocationAvailability la1 = createLocationAvailability(p1, 37.5665, 126.9780);
        LocationAvailability la2 = createLocationAvailability(p2, 37.4979, 127.0276);

        given(participantRepository.findByMeeting(meeting)).willReturn(participants);
        given(locationAvailabilityRepository.findByParticipantIn(anyList()))
                .willReturn(List.of(la1, la2));
        given(transitClient.getTravelTimeMinutes(any(), any())).willReturn(20.0);
        given(poiClient.searchNearby(any()))
                .willReturn(List.of(new PoiPlace("테스트 카페", "서울시 어딘가", 37.5, 127.0)));
        given(placeCandidateRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(meetingResultRepository.findByMeeting(meeting)).willReturn(Optional.empty());
        given(meetingResultRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        placeRecommendService.recommendAndSave(meeting);

        // then
        verify(placeCandidateRepository, atLeastOnce()).save(any(PlaceCandidate.class));
        verify(placeTravelInfoRepository, atLeastOnce()).save(any());
        verify(meetingResultRepository, times(1)).findByMeeting(meeting);
        verify(meetingResultRepository, atLeastOnce()).save(any(MeetingResult.class));
    }

    @Test
    @DisplayName("참여자가 2명 미만인 경우 예외 발생")
    void recommendAndSave_TooFewParticipants() {
        // given
        Meeting meeting = createMeeting(1L, "CODE");
        List<Participant> participants = List.of(createParticipant(1L, meeting, "A"));
        given(participantRepository.findByMeeting(meeting)).willReturn(participants);

        // when & then
        assertThat(
                        org.junit.jupiter.api.Assertions.assertThrows(
                                IllegalArgumentException.class,
                                () -> placeRecommendService.recommendAndSave(meeting)))
                .hasMessageContaining("참여자가 2명 이상 필요합니다.");
    }

    @Test
    @DisplayName("출발지를 등록하지 않은 참여자가 있는 경우 예외 발생")
    void recommendAndSave_MissingLocation() {
        // given
        Meeting meeting = createMeeting(1L, "CODE");
        Participant p1 = createParticipant(1L, meeting, "A");
        Participant p2 = createParticipant(2L, meeting, "B");

        // p1만 LocationAvailability가 있고 p2는 없음
        LocationAvailability la1 = createLocationAvailability(p1, 37.5, 127.0);

        given(participantRepository.findByMeeting(meeting)).willReturn(List.of(p1, p2));
        given(locationAvailabilityRepository.findByParticipantIn(anyList()))
                .willReturn(List.of(la1));

        // when & then
        assertThat(
                        org.junit.jupiter.api.Assertions.assertThrows(
                                IllegalStateException.class,
                                () -> placeRecommendService.recommendAndSave(meeting)))
                .hasMessageContaining("출발지를 등록하지 않은 참여자가 있습니다");
    }

    private Meeting createMeeting(Long id, String code) {
        Meeting meeting = Meeting.builder().code(code).name("Meeting").build();
        ReflectionTestUtils.setField(meeting, "id", id);
        return meeting;
    }

    private Participant createParticipant(Long id, Meeting meeting, String nick) {
        Participant p = Participant.create(meeting, nick, "token-" + id);
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    private LocationAvailability createLocationAvailability(
            Participant participant, double lat, double lon) {
        return LocationAvailability.create(participant, "주소", lat, lon);
    }
}
