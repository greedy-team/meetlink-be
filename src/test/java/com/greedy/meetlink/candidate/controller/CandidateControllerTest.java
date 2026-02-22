package com.greedy.meetlink.candidate.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.greedy.meetlink.candidate.dto.response.PlaceCandidateResponse;
import com.greedy.meetlink.candidate.service.PlaceCandidateService;
import com.greedy.meetlink.candidate.service.TimeCandidateService;
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
@DisplayName("CandidateController 테스트")
class CandidateControllerTest {

    MockMvc mockMvc;

    @Autowired WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @MockitoBean TimeCandidateService timeCandidateService;
    @MockitoBean PlaceCandidateService placeCandidateService;

    @Nested
    @DisplayName("POST /meetings/{code}/candidates/place - 추천 장소 계산")
    class Calculate {

        @Test
        @DisplayName("정상 요청 시 200 및 결과 반환")
        void calculate_success() throws Exception {
            given(placeCandidateService.calculate("ABC123")).willReturn(fakePlaceCandidates());

            mockMvc.perform(
                            post("/meetings/ABC123/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.result").isArray());

            verify(placeCandidateService).calculate("ABC123");
        }

        @Test
        @DisplayName("존재하지 않는 모임 코드 → 404")
        void calculate_meetingNotFound() throws Exception {
            willThrow(new MeetingNotFoundException())
                    .given(placeCandidateService)
                    .calculate("INVALID");

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
                    .given(placeCandidateService)
                    .calculate("ABC123");

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
                    .given(placeCandidateService)
                    .calculate("ABC123");

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
                    .given(placeCandidateService)
                    .calculate("ABC123");

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
        @DisplayName("정상 조회 시 배열 반환")
        void getPlace_success() throws Exception {
            given(placeCandidateService.get("ABC123")).willReturn(fakePlaceCandidates());

            mockMvc.perform(
                            get("/meetings/ABC123/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result.length()").value(2))
                    .andExpect(jsonPath("$.result[0].name").value("스타벅스 강남점"))
                    .andExpect(jsonPath("$.result[0].address").value("서울 강남구 테헤란로 101"))
                    .andExpect(jsonPath("$.result[0].rank").value(1))
                    .andExpect(jsonPath("$.result[1].rank").value(2));
        }

        @Test
        @DisplayName("계산된 후보 없음 → 빈 배열 반환")
        void getPlace_empty() throws Exception {
            given(placeCandidateService.get("ABC123")).willReturn(List.of());

            mockMvc.perform(
                            get("/meetings/ABC123/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result.length()").value(0));
        }

        @Test
        @DisplayName("존재하지 않는 모임 코드 → 404")
        void getPlace_meetingNotFound() throws Exception {
            willThrow(new MeetingNotFoundException()).given(placeCandidateService).get("INVALID");

            mockMvc.perform(
                            get("/meetings/INVALID/candidates/place")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    private List<PlaceCandidateResponse> fakePlaceCandidates() {
        return List.of(
                PlaceCandidateResponse.builder()
                        .id(1L)
                        .name("스타벅스 강남점")
                        .address("서울 강남구 테헤란로 101")
                        .latitude(37.4979)
                        .longitude(127.0276)
                        .rank(1)
                        .avgTravelTime(25.5)
                        .maxTravelTime(40.0)
                        .build(),
                PlaceCandidateResponse.builder()
                        .id(2L)
                        .name("투썸플레이스 역삼점")
                        .address("서울 강남구 역삼로 99")
                        .latitude(37.4985)
                        .longitude(127.0312)
                        .rank(2)
                        .avgTravelTime(31.0)
                        .maxTravelTime(40.0)
                        .build());
    }
}
