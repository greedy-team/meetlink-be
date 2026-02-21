package com.greedy.meetlink.place.service;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.candidate.PlaceCandidateRepository;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.place.algorithm.*;
import com.greedy.meetlink.place.algorithm.CandidateFilter.FilteredCandidate;
import com.greedy.meetlink.place.algorithm.CandidateFilter.ParticipantTravelTime;
import com.greedy.meetlink.place.algorithm.CandidateScorer.ScoredCandidate;
import com.greedy.meetlink.place.algorithm.PlaceReevaluator.ReevaluatedPlace;
import com.greedy.meetlink.place.client.TMapPoiClient;
import com.greedy.meetlink.place.client.TransitClient;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse.PoiPlace;
import com.greedy.meetlink.place.domain.Coordinate;
import com.greedy.meetlink.result.PlaceTravelInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceRecommendationServiceTest {

    @Mock private MeetingRepository meetingRepository;
    @Mock private LocationAvailabilityRepository locationAvailabilityRepository;
    @Mock private PlaceCandidateRepository placeCandidateRepository;
    @Mock private PlaceTravelInfoRepository placeTravelInfoRepository;

    // [수정] 구체 클래스 대신 인터페이스 Mock 사용
    @Mock private TransitClient transitClient;
    @Mock private TMapPoiClient tMapPoiClient;

    private PlaceRecommendationService placeRecommendationService;

    private GeometricMedianCalculator geometricMedianCalculator;
    private PolarSamplingGenerator polarSamplingGenerator;
    private CandidateFilter candidateFilter;
    private CandidateScorer candidateScorer;
    private PlaceReevaluator placeReevaluator;

    @BeforeEach
    void setUp() {
        geometricMedianCalculator = new GeometricMedianCalculator();
        polarSamplingGenerator = new PolarSamplingGenerator();
        candidateFilter = new CandidateFilter(transitClient);
        
        // [수정] CandidateScorer는 기본 생성자 사용
        candidateScorer = new CandidateScorer();
        
        placeReevaluator = new PlaceReevaluator(tMapPoiClient);

        placeRecommendationService = new PlaceRecommendationService(
                meetingRepository,
                locationAvailabilityRepository,
                placeCandidateRepository,
                placeTravelInfoRepository,
                geometricMedianCalculator,
                polarSamplingGenerator,
                candidateFilter,
                candidateScorer,
                placeReevaluator
        );
    }

    @Test
    @DisplayName("[1. 기하중심] 참여자들의 중간 지점이 계산되어야 한다")
    void calculateGeometricMedian() {
        List<Coordinate> coords = List.of(
                new Coordinate(0, 0),
                new Coordinate(10, 0),
                new Coordinate(0, 10)
        );

        Coordinate center = geometricMedianCalculator.calculate(coords);

        assertThat(center.latitude()).isBetween(0.0, 10.0);
        assertThat(center.longitude()).isBetween(0.0, 10.0);
    }

    @Test
    @DisplayName("[2. 후보생성] 중심점을 기준으로 후보지가 생성되어야 한다")
    void generateCandidates() {
        Coordinate center = new Coordinate(37.5, 127.0);
        List<Coordinate> participants = List.of(new Coordinate(37.6, 127.1));

        List<Coordinate> candidates = polarSamplingGenerator.generate(center, participants);

        assertThat(candidates).isNotEmpty();
        assertThat(candidates).contains(center);
    }

    @Test
    @DisplayName("[3. 필터링] 2차 필터(시간)에서 이동 시간이 계산되어야 한다")
    void filterByTravelTime() {
        Coordinate center = new Coordinate(37.5, 127.0);
        List<Coordinate> participants = List.of(new Coordinate(37.6, 127.1));
        
        List<Coordinate> candidates = List.of(
                new Coordinate(37.51, 127.01),
                new Coordinate(37.52, 127.02)
        );

        given(transitClient.getTravelTimeMinutes(any(), any())).willReturn(30.0);

        List<FilteredCandidate> result = candidateFilter.filterByTravelTime(candidates, participants, center);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).participantTravelTimes().get(0).travelTimeMinutes()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("[4. 장소매핑] 점수가 매겨진 후보를 실제 POI와 매핑해야 한다 (재평가 X)")
    void matchWithRealPlaces() {
        Coordinate coord = new Coordinate(37.5, 127.0);
        FilteredCandidate filtered = new FilteredCandidate(coord, List.of(
            new ParticipantTravelTime(new Coordinate(0,0), 30.0)
        ));
        // [수정] rank 인자 추가
        ScoredCandidate scored = new ScoredCandidate(filtered, 30.0, 40.0, 10.0, 80.0, 0);
        
        List<ScoredCandidate> candidates = List.of(scored);

        given(tMapPoiClient.searchNearby(coord)).willReturn(List.of(
                new PoiPlace("Test Cafe", "Test Address", 37.501, 127.001)
        ));

        List<ReevaluatedPlace> result = placeReevaluator.matchWithRealPlaces(candidates);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).searchResult().name()).isEqualTo("Test Cafe");
        assertThat(result.get(0).avgTravelTime()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("[통합] 전체 프로세스: 상위 3개 선정 및 저장 검증")
    void recommend_fullProcess() {
        String meetingCode = "TEST";
        Meeting meeting = Meeting.builder().code(meetingCode).name("M").build();
        
        List<LocationAvailability> locations = List.of(
            createLocation(new Coordinate(0, 0), "A"),
            createLocation(new Coordinate(10, 0), "B"),
            createLocation(new Coordinate(0, 10), "C")
        );

        given(meetingRepository.findByCode(meetingCode)).willReturn(Optional.of(meeting));
        given(locationAvailabilityRepository.findByMeetingCode(meetingCode)).willReturn(locations);

        given(transitClient.getTravelTimeMinutes(any(), any())).willReturn(30.0);
        
        given(tMapPoiClient.searchNearby(any())).willReturn(List.of(
                new PoiPlace("Cafe", "Addr", 0, 0)
        ));

        placeRecommendationService.recommend(meetingCode);

        verify(placeCandidateRepository, atLeastOnce()).save(any());
        verify(placeTravelInfoRepository, atLeastOnce()).save(any());
        
        verify(transitClient, atMost(9)).getTravelTimeMinutes(any(), any());
    }

    private LocationAvailability createLocation(Coordinate c, String name) {
        Participant p = Participant.builder().nickname(name).build();
        return LocationAvailability.builder()
                .participant(p)
                .latitude(c.latitude())
                .longitude(c.longitude())
                .build();
    }
}
