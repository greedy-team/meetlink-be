package com.greedy.meetlink.availability.controller;

import com.greedy.meetlink.availability.dto.request.LocationAvailabilityRequest;
import com.greedy.meetlink.availability.dto.request.TimeAvailabilityRequest;
import com.greedy.meetlink.availability.dto.response.LocationAvailabilityResponse;
import com.greedy.meetlink.availability.dto.response.TimeAvailabilitiesResponse;
import com.greedy.meetlink.availability.dto.response.TimeAvailabilityResponse;
import com.greedy.meetlink.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Availability", description = "참여자 시간 / 장소 API")
public interface AvailabilityControllerSpec {
    @Operation(
            summary = "시간 입력",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                    @Content(
                                            examples = {
                                                @ExampleObject(
                                                        name = "특정 날짜 기반 모임",
                                                        value =
                                                                """
                                                                        {
                                                                          "availabilities": [
                                                                            {
                                                                              "date": "2026-02-21",
                                                                              "startTimes": ["10:00", "10:30", "11:00"]
                                                                            },
                                                                            {
                                                                              "date": "2026-02-22",
                                                                              "startTimes": ["14:00", "14:30"]
                                                                            }
                                                                          ]
                                                                        }
                                            """),
                                                @ExampleObject(
                                                        name = "매주 기반 모임",
                                                        value =
                                                                """
                                                                        {
                                                                          "availabilities": [
                                                                            {
                                                                              "dayOfWeek": 0,
                                                                              "startTimes": ["10:00", "10:30", "11:00"]
                                                                            },
                                                                            {
                                                                              "dayOfWeek": 1,
                                                                              "startTimes": ["14:00", "14:30"]
                                                                            }
                                                                          ]
                                                                        }
                                            """),
                                            })))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "입력 성공",
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
                responseCode = "400",
                description = "잘못된 요청",
                content =
                        @Content(
                                examples = {
                                    @ExampleObject(
                                            name = "모임 타입 불일치",
                                            value =
                                                    """
                                        {
                                            "status": false,
                                            "code": "INVALID_TIME_AVAILABILITY",
                                            "message": "날짜 기반 모임은 날짜 기반 입력만 가능합니다."
                                        }
                                    """),
                                    @ExampleObject(
                                            name = "잘못된 요청 형식",
                                            value =
                                                    """
                                        {
                                            "status": false,
                                            "code": "INVALID_REQUEST_BODY",
                                            "message": "요청 본문이 비어있거나 형식이 잘못되었습니다."
                                        }
                                    """)
                                }))
    })
    ApiResponse<Void> submitTime(
            @PathVariable String code,
            @RequestHeader("X-Participant-Token") String token,
            @Valid @RequestBody TimeAvailabilityRequest request);

    @Operation(summary = "전체 참여자 입력 시간 조회")
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
                                                        "availabilities": [
                                                            {
                                                                "date": "2026-02-21",
                                                                "startTimes": [
                                                                    "09:00:00",
                                                                    "09:30:00",
                                                                    "12:30:00"
                                                                ]
                                                            }
                                                        ],
                                                        "nickname": "테스트1"
                                                    },
                                                    {
                                                        "availabilities": [
                                                            {
                                                                "date": "2026-02-22",
                                                                "startTimes": [
                                                                    "09:00:00",
                                                                    "09:30:00",
                                                                    "12:30:00"
                                                                ]
                                                            }
                                                        ],
                                                        "nickname": "테스트2"
                                                    }
                                                ]
                                            }
                                            """)))
    })
    ApiResponse<List<TimeAvailabilitiesResponse>> getTimeAvailabilities(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token);

    @Operation(summary = "내 입력 시간 조회")
    @ApiResponses(
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
                                                "date": "2026-02-22",
                                                "startTimes": [
                                                    "09:00:00",
                                                    "09:30:00",
                                                    "12:30:00"
                                                ]
                                            }
                                        ]
                                    }
                                    """))))
    ApiResponse<List<TimeAvailabilityResponse>> getMyTimeAvailability(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token);

    @Operation(
            summary = "장소 입력",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                    @Content(
                                            examples =
                                                    @ExampleObject(
                                                            value =
                                                                    """
                                            {
                                                 "name": "세종대학교",
                                                 "address": "서울 광진구 능동로 209",
                                                 "latitude": 37.551604,
                                                 "longitude": 127.073177
                                             }
                                            """))))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "입력 성공",
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
                responseCode = "400",
                description = "잘못된 요청",
                content =
                        @Content(
                                examples = {
                                    @ExampleObject(
                                            name = "유효성 검사 실패",
                                            value =
                                                    """
                                        {
                                            "status": false,
                                            "code": "VALIDATION_FAILED",
                                            "message": "요청 값이 올바르지 않습니다.",
                                            "result": {
                                                "latitude": "위도는 필수입니다."
                                            }
                                        }
                                    """),
                                    @ExampleObject(
                                            name = "잘못된 요청 형식",
                                            value =
                                                    """
                                        {
                                            "status": false,
                                            "code": "INVALID_REQUEST_BODY",
                                            "message": "요청 본문이 비어있거나 형식이 잘못되었습니다."
                                        }
                                    """)
                                }))
    })
    ApiResponse<Void> submitLocation(
            @PathVariable String code,
            @RequestHeader("X-Participant-Token") String token,
            @Valid @RequestBody LocationAvailabilityRequest request);

    @Operation(summary = "내 입력 장소 조회")
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
                                                     "name": "세종대학교",
                                                     "address": "서울 광진구 능동로 209",
                                                     "latitude": 37.551604,
                                                     "longitude": 127.073177
                                                 }
                                             }
                                            """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "장소 입력 정보 없음",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                            {
                                                "code": "LOCATION_NOT_SUBMITTED",
                                                "message": "아직 장소를 입력하지 않았습니다.",
                                                "status": false
                                            }
                                            """)))
    })
    ApiResponse<LocationAvailabilityResponse> getMyLocationAvailability(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token);
}
