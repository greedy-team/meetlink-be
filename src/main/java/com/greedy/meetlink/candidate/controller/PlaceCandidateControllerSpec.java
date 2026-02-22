package com.greedy.meetlink.candidate.controller;

import com.greedy.meetlink.candidate.dto.response.PlaceCandidateListResponse;
import com.greedy.meetlink.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Place Candidate", description = "장소 후보 API")
public interface PlaceCandidateControllerSpec {

    @Operation(
            summary = "추천 장소 계산",
            description = "현재 참여자들의 위치 데이터를 기반으로 추천 장소 후보를 새로 계산하고 저장합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "계산 성공",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                        {
                            "status": true
                        }
                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "모임 없음",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                        {
                            "status": false,
                            "code": "NOT_FOUND",
                            "message": "모임을 찾을 수 없습니다."
                        }
                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "계산 불가 (참여자 부족 또는 좌표 미등록)",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                        {
                            "status": false,
                            "code": "ILLEGAL_STATE",
                            "message": "출발지를 등록하지 않은 참여자가 있습니다: [홍길동]"
                        }
                        """)))
    })
    ApiResponse<Void> calculate(@PathVariable String code);

    @Operation(summary = "추천 장소 조회", description = "해당 모임에 대해 계산된 추천 장소 후보 목록을 조회합니다.")
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
                            "result": {
                                "recommendedPlaces": [
                                    {
                                        "placeName": "스타벅스 강남점",
                                        "roadAddress": "서울 강남구 테헤란로 101",
                                        "latitude": 37.4979,
                                        "longitude": 127.0276,
                                        "rank": 1,
                                        "averageDuration": 1530,
                                        "maxDuration": 2400,
                                        "participantDurations": []
                                    }
                                ]
                            }
                        }
                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "모임 없음",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                        {
                            "status": false,
                            "code": "NOT_FOUND",
                            "message": "모임을 찾을 수 없습니다."
                        }
                        """)))
    })
    ApiResponse<PlaceCandidateListResponse> getCandidates(@PathVariable String code);
}
