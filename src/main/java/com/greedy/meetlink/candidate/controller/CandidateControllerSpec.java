package com.greedy.meetlink.candidate.controller;

import com.greedy.meetlink.candidate.dto.response.PlaceCandidateResponse;
import com.greedy.meetlink.candidate.dto.response.TimeCandidateResponse;
import com.greedy.meetlink.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Candidate", description = "후보 API")
public interface CandidateControllerSpec {
    @Operation(summary = "시간 후보 조회", description = "해당 모임에 대해 계산된 시간 후보 목록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                {
                                    "status": true,
                                    "result": [
                                        {
                                            "availableCount": 2,
                                            "date": "2026-02-21",
                                            "dayOfWeek": null,
                                            "endTime": "10:00:00",
                                            "id": 3,
                                            "rank": 1,
                                            "startTime": "09:00:00"
                                        },
                                        {
                                            "availableCount": 1,
                                            "date": "2026-02-21",
                                            "dayOfWeek": null,
                                            "endTime": "12:00:00",
                                            "id": 4,
                                            "rank": 2,
                                            "startTime": "11:30:00"
                                        },
                                        {
                                            "availableCount": 1,
                                            "date": "2026-02-21",
                                            "dayOfWeek": null,
                                            "endTime": "13:00:00",
                                            "id": 5,
                                            "rank": 3,
                                            "startTime": "12:30:00"
                                        }
                                    ]
                                }
                                """)))
    })
    ApiResponse<List<TimeCandidateResponse>> getTimeCandidates(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token);

    @Operation(summary = "장소 후보 조회", description = "해당 모임에 대해 계산된 장소 후보 목록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                                {
                                                                    "status": true,
                                                                    "result": [
                                                                        {
                                                                            "address": "서울 서초구 동산로 3",
                                                                            "avgTravelTime": 2760.0,
                                                                            "id": 22,
                                                                            "latitude": 37.46906940866943,
                                                                            "longitude": 127.04028626682705,
                                                                            "maxTravelTime": 2880.0,
                                                                            "name": "메가MGC커피 양재시민의숲점",
                                                                            "rank": 1
                                                                        },
                                                                        {
                                                                            "address": "서울 서초구 바우뫼로33길 7-14",
                                                                            "avgTravelTime": 2940.0,
                                                                            "id": 23,
                                                                            "latitude": 37.4776697747743,
                                                                            "longitude": 127.03683815428,
                                                                            "maxTravelTime": 3060.0,
                                                                            "name": "유어마이커피브루어스",
                                                                            "rank": 2
                                                                        },
                                                                        {
                                                                            "address": "서울 서초구 과천대로 870-13",
                                                                            "avgTravelTime": 2940.0,
                                                                            "id": 24,
                                                                            "latitude": 37.4690396245957,
                                                                            "longitude": 126.987983532889,
                                                                            "maxTravelTime": 3240.0,
                                                                            "name": "모닝해즈 CJENM커머스부문점",
                                                                            "rank": 3
                                                                        }
                                                                    ]
                                                                }
                        """)))
    })
    ApiResponse<List<PlaceCandidateResponse>> getPlaceCandidates(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token);
}
