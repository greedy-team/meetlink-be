package com.greedy.meetlink.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

    @Mock private TransitClient transitClient;
    @Mock private PoiClient poiClient;

    private PlaceRecommendService placeRecommendService;

    private GeometricMedianCalculator geometricMedianCalculator;
    private PolarSamplingGenerator polarSamplingGenerator;
    private CandidateFilter candidateFilter;
    private CandidateScorer candidateScorer;
    private PlaceMapper placeMapper;

    @BeforeEach
    void setUp() {
        geometricMedianCalculator = new GeometricMedianCalculator();
        polarSamplingGenerator = new PolarSamplingGenerator();

        // CandidateFilter 생성 및 @Value 주입
        candidateFilter = new CandidateFilter(transitClient);
        ReflectionTestUtils.setField(candidateFilter, "tmapCallDelayMs", 0L);

        ScoreCalculator scoreCalculator = new ScoreCalculator();
        candidateScorer = new CandidateScorer(scoreCalculator);
        placeMapper = new PlaceMapper(poiClient);

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
                        participantRepository);
    }

    @Test
    @DisplayName("장소 추천 및 저장 전체 프로세스 검증")
    void recommendAndSave_Success() {
        // given
        Meeting meeting = createMeeting(1L, "TEST-CODE");
        List<Participant> participants =
                List.of(
                        createParticipant(1L, meeting, "UserA", 37.5665, 126.9780), // 시청
                        createParticipant(2L, meeting, "UserB", 37.4979, 127.0276) // 강남역
                        );

        given(participantRepository.findByMeeting(meeting)).willReturn(participants);

        // Mock TransitClient: 항상 20분 소요
        given(transitClient.getTravelTimeMinutes(any(), any())).willReturn(20.0);

        // Mock PoiClient: 가짜 장소 반환
        given(poiClient.searchNearby(any()))
                .willReturn(List.of(new PoiPlace("테스트 카페", "서울시 어딘가", 37.5, 127.0)));

        // Mock Repositories
        given(placeCandidateRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(meetingResultRepository.findByMeeting(meeting)).willReturn(Optional.empty());
        given(meetingResultRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        placeRecommendService.recommendAndSave(meeting);

        // then
        // 1. 후보지 저장 확인
        verify(placeCandidateRepository, atLeastOnce()).save(any(PlaceCandidate.class));

        // 2. 여행 정보 저장 확인
        verify(placeTravelInfoRepository, atLeastOnce()).save(any());

        // 3. 최종 결과 연결 (MeetingResult)
        verify(meetingResultRepository, times(1)).findByMeeting(meeting);
        verify(meetingResultRepository, atLeastOnce()).save(any(MeetingResult.class));
    }

    @Test
    @DisplayName("참여자가 2명 미만인 경우 예외 발생")
    void recommendAndSave_TooFewParticipants() {
        // given
        Meeting meeting = createMeeting(1L, "CODE");
        List<Participant> participants = List.of(createParticipant(1L, meeting, "A", 37.5, 127.0));
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
        Participant p1 = createParticipant(1L, meeting, "A", 37.5, 127.0);
        Participant p2 = Participant.create(meeting, "B", "token"); // 좌표 없음

        given(participantRepository.findByMeeting(meeting)).willReturn(List.of(p1, p2));

        // when & then
        assertThat(
                        org.junit.jupiter.api.Assertions.assertThrows(
                                IllegalStateException.class,
                                () -> placeRecommendService.recommendAndSave(meeting)))
                .hasMessageContaining("출발지를 등록하지 않은 참여자가 있습니다");
    }

    // Helper methods
    private Meeting createMeeting(Long id, String code) {
        Meeting meeting = Meeting.builder().code(code).name("Meeting").build();
        ReflectionTestUtils.setField(meeting, "id", id);
        return meeting;
    }

    private Participant createParticipant(
            Long id, Meeting meeting, String nick, double lat, double lon) {
        Participant p = Participant.create(meeting, nick, "token-" + id);
        p.updateLocation(lat, lon);
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }
}
