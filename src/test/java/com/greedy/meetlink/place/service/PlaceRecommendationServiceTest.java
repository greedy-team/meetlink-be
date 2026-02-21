package com.greedy.meetlink.place.service;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.candidate.PlaceCandidateRepository;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.place.algorithm.*;
import com.greedy.meetlink.place.client.TMapPoiClient;
import com.greedy.meetlink.place.client.TMapTransitClient;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceRecommendationServiceTest {

    @Mock private MeetingRepository meetingRepository;
    @Mock private LocationAvailabilityRepository locationAvailabilityRepository;
    @Mock private PlaceCandidateRepository placeCandidateRepository;
    @Mock private PlaceTravelInfoRepository placeTravelInfoRepository;
    @Mock private TMapTransitClient tMapTransitClient;
    @Mock private TMapPoiClient tMapPoiClient;

    private PlaceRecommendationService placeRecommendationService;

    @BeforeEach
    void setUp() {
        GeometricMedianCalculator geometricMedianCalculator = new GeometricMedianCalculator();
        PolarSamplingGenerator polarSamplingGenerator = new PolarSamplingGenerator();
        CandidateFilter candidateFilter = new CandidateFilter(tMapTransitClient);
        ScoreCalculator scoreCalculator = new ScoreCalculator();
        CandidateScorer candidateScorer = new CandidateScorer(scoreCalculator);
        PlaceReevaluator placeReevaluator = new PlaceReevaluator(tMapPoiClient, tMapTransitClient, scoreCalculator);

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
    @DisplayName("참여자 3명(서울) 위치로 장소 추천 시나리오 검증")
    void recommend_success() {
        // given
        String meetingCode = "TEST-CODE";
        // Meeting 생성 (Builder 사용)
        Meeting meeting = Meeting.builder()
                .code(meetingCode)
                .name("Test Meeting")
                .build();

        // 서울 시내 주요 지점 좌표
        Coordinate c1 = new Coordinate(37.5665, 126.9780); // 시청
        Coordinate c2 = new Coordinate(37.5116, 127.0592); // 코엑스
        Coordinate c3 = new Coordinate(37.5545, 126.9707); // 서울역

        // Participant 생성
        Participant p1 = Participant.builder().meeting(meeting).nickname("UserA").token("t1").build();
        Participant p2 = Participant.builder().meeting(meeting).nickname("UserB").token("t2").build();
        Participant p3 = Participant.builder().meeting(meeting).nickname("UserC").token("t3").build();

        // LocationAvailability 생성
        List<LocationAvailability> locations = List.of(
                LocationAvailability.builder().participant(p1).latitude(c1.latitude()).longitude(c1.longitude()).address("시청").build(),
                LocationAvailability.builder().participant(p2).latitude(c2.latitude()).longitude(c2.longitude()).address("코엑스").build(),
                LocationAvailability.builder().participant(p3).latitude(c3.latitude()).longitude(c3.longitude()).address("서울역").build()
        );

        given(meetingRepository.findByCode(meetingCode)).willReturn(Optional.of(meeting));
        given(locationAvailabilityRepository.findByMeetingCode(meetingCode)).willReturn(locations);

        // Mock: 이동 시간 = 직선 거리(km) * 10분 (가상 로직)
        // distanceTo()는 Haversine 공식 사용 가정
        given(tMapTransitClient.getTravelTimeMinutes(any(Coordinate.class), any(Coordinate.class)))
                .willAnswer(invocation -> {
                    Coordinate start = invocation.getArgument(0);
                    Coordinate end = invocation.getArgument(1);
                    double dist = start.distanceTo(end);
                    return dist * 10.0;
                });

        // Mock: POI 검색 (검색된 좌표 주변에 가상의 카페가 있다고 가정)
        given(tMapPoiClient.searchNearby(any(Coordinate.class)))
                .willAnswer(invocation -> {
                    Coordinate center = invocation.getArgument(0);
                    return List.of(new PoiPlace("추천 카페", "서울시 어딘가", center.latitude(), center.longitude()));
                });

        // when
        placeRecommendationService.recommend(meetingCode);

        // then
        // 최종적으로 후보지가 저장되었는지 검증 (최소 1번 이상 호출)
        verify(placeCandidateRepository, atLeastOnce()).save(any());
        verify(placeTravelInfoRepository, atLeastOnce()).save(any());
    }
}
