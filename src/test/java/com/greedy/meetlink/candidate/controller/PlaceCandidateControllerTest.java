package com.greedy.meetlink.candidate.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.greedy.meetlink.candidate.dto.response.ParticipantDurationResponse;
import com.greedy.meetlink.candidate.dto.response.PlaceCandidateListResponse;
import com.greedy.meetlink.candidate.dto.response.RecommendedPlaceResponse;
import com.greedy.meetlink.candidate.service.PlaceCandidateQueryService;
import com.greedy.meetlink.candidate.service.PlaceRecommendService;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PlaceCandidateController 테스트")
class PlaceCandidateControllerTest {

    MockMvc mockMvc;

    @Autowired WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @MockitoBean PlaceRecommendService placeRecommendService;

    @MockitoBean PlaceCandidateQueryService placeCandidateQueryService;

    @Nested
    @DisplayName("POST /meetings/{code}/candidates/place - 추천 장소 계산")
    class Calculate {

        @Test
        @DisplayName("정상 요청 시 200 반환")
        void calculate_success() throws Exception {
            willDoNothing().given(placeRecommendService).recommendAndSave("ABC123");

            mockMvc.perform(
                            post("/meetings/ABC123/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true));

            verify(placeRecommendService).recommendAndSave("ABC123");
        }

        @Test
        @DisplayName("존재하지 않는 모임 코드 → 404")
        void calculate_meetingNotFound() throws Exception {
            willThrow(new MeetingNotFoundException())
                    .given(placeRecommendService)
                    .recommendAndSave("INVALID");

            mockMvc.perform(
                            post("/meetings/INVALID/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("참여자 부족 → 400")
        void calculate_notEnoughParticipants() throws Exception {
            willThrow(new IllegalArgumentException("장소 추천을 위해 참여자가 2명 이상 필요합니다."))
                    .given(placeRecommendService)
                    .recommendAndSave("ABC123");

            mockMvc.perform(
                            post("/meetings/ABC123/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("좌표 미등록 참여자 존재 → 400")
        void calculate_participantMissingLocation() throws Exception {
            willThrow(new IllegalStateException("출발지를 등록하지 않은 참여자가 있습니다: [홍길동]"))
                    .given(placeRecommendService)
                    .recommendAndSave("ABC123");

            mockMvc.perform(
                            post("/meetings/ABC123/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("추천 가능한 장소 없음 → 400")
        void calculate_noRecommendablePlace() throws Exception {
            willThrow(new IllegalStateException("추천 가능한 장소를 찾지 못했습니다."))
                    .given(placeRecommendService)
                    .recommendAndSave("ABC123");

            mockMvc.perform(
                            post("/meetings/ABC123/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /meetings/{code}/candidates/place - 추천 장소 조회")
    class GetCandidates {

        @Test
        @DisplayName("정상 조회 시 recommendedPlaces 배열 반환")
        void getCandidates_success() throws Exception {
            given(placeCandidateQueryService.getCandidates("ABC123"))
                    .willReturn(fakePlaceCandidateListResponse());

            mockMvc.perform(
                            get("/meetings/ABC123/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.result.recommendedPlaces").isArray())
                    .andExpect(jsonPath("$.result.recommendedPlaces.length()").value(2))
                    .andExpect(
                            jsonPath("$.result.recommendedPlaces[0].placeName").value("스타벅스 강남점"))
                    .andExpect(
                            jsonPath("$.result.recommendedPlaces[0].roadAddress")
                                    .value("서울 강남구 테헤란로 101"))
                    .andExpect(jsonPath("$.result.recommendedPlaces[0].latitude").value(37.4979))
                    .andExpect(jsonPath("$.result.recommendedPlaces[0].longitude").value(127.0276))
                    .andExpect(jsonPath("$.result.recommendedPlaces[0].rank").value(1))
                    .andExpect(
                            jsonPath("$.result.recommendedPlaces[0].averageDuration").value(1530))
                    .andExpect(jsonPath("$.result.recommendedPlaces[0].maxDuration").value(2400))
                    .andExpect(
                            jsonPath("$.result.recommendedPlaces[0].participantDurations")
                                    .isArray())
                    .andExpect(
                            jsonPath("$.result.recommendedPlaces[0].participantDurations.length()")
                                    .value(2))
                    .andExpect(
                            jsonPath(
                                            "$.result.recommendedPlaces[0].participantDurations[0].nickname")
                                    .value("홍길동"))
                    .andExpect(
                            jsonPath(
                                            "$.result.recommendedPlaces[0].participantDurations[0].duration")
                                    .value(1500))
                    .andExpect(jsonPath("$.result.recommendedPlaces[1].rank").value(2));
        }

        @Test
        @DisplayName("계산된 후보 없음 → 빈 배열 반환")
        void getCandidates_empty() throws Exception {
            given(placeCandidateQueryService.getCandidates("ABC123"))
                    .willReturn(PlaceCandidateListResponse.of(List.of()));

            mockMvc.perform(
                            get("/meetings/ABC123/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.result.recommendedPlaces").isArray())
                    .andExpect(jsonPath("$.result.recommendedPlaces.length()").value(0));
        }

        @Test
        @DisplayName("존재하지 않는 모임 코드 → 404")
        void getCandidates_meetingNotFound() throws Exception {
            willThrow(new MeetingNotFoundException())
                    .given(placeCandidateQueryService)
                    .getCandidates("INVALID");

            mockMvc.perform(
                            get("/meetings/INVALID/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("participantDurations pathData null 허용")
        void getCandidates_pathDataNull() throws Exception {
            given(placeCandidateQueryService.getCandidates("ABC123"))
                    .willReturn(fakePlaceCandidateListResponseWithNullPath());

            mockMvc.perform(
                            get("/meetings/ABC123/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath(
                                            "$.result.recommendedPlaces[0].participantDurations[0].pathData")
                                    .doesNotExist());
        }
    }

    private PlaceCandidateListResponse fakePlaceCandidateListResponse() {
        List<ParticipantDurationResponse> participants1 =
                List.of(
                        new ParticipantDurationResponse("홍길동", 1500, "polyline_data_1"),
                        new ParticipantDurationResponse("김철수", 1560, "polyline_data_2"));
        List<ParticipantDurationResponse> participants2 =
                List.of(
                        new ParticipantDurationResponse("홍길동", 1800, "polyline_data_3"),
                        new ParticipantDurationResponse("김철수", 1920, "polyline_data_4"));

        List<RecommendedPlaceResponse> places =
                List.of(
                        new RecommendedPlaceResponse(
                                "스타벅스 강남점",
                                "서울 강남구 테헤란로 101",
                                37.4979,
                                127.0276,
                                1,
                                1530,
                                2400,
                                participants1),
                        new RecommendedPlaceResponse(
                                "투썸플레이스 역삼점",
                                "서울 강남구 역삼로 99",
                                37.4985,
                                127.0312,
                                2,
                                1860,
                                2400,
                                participants2));

        return PlaceCandidateListResponse.of(places);
    }

    private PlaceCandidateListResponse fakePlaceCandidateListResponseWithNullPath() {
        List<ParticipantDurationResponse> participants =
                List.of(new ParticipantDurationResponse("홍길동", 1500, null));
        List<RecommendedPlaceResponse> places =
                List.of(
                        new RecommendedPlaceResponse(
                                "스타벅스 강남점",
                                "서울 강남구 테헤란로 101",
                                37.4979,
                                127.0276,
                                1,
                                1530,
                                2400,
                                participants));
        return PlaceCandidateListResponse.of(places);
    }
}
