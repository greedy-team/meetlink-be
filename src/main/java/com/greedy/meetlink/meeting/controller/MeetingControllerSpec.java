package com.greedy.meetlink.meeting.controller;

import com.greedy.meetlink.common.ApiResponse;
import com.greedy.meetlink.meeting.dto.request.MeetingCreateRequest;
import com.greedy.meetlink.meeting.dto.request.MeetingUpdateRequest;
import com.greedy.meetlink.meeting.dto.response.MeetingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Meeting", description = "모임 API")
public interface MeetingControllerSpec {
    @Operation(summary = "모임 조회")
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
                                        "id": 1,
                                        "name": "MeetLink 팀 회의",
                                        "code": "A1B2C3",
                                        "enableTimeRecommendation": true,
                                        "enablePlaceRecommendation": false,
                                        "timeAvailabilityType": "WEEKLY",
                                        "timeRangeStart": "09:00:00",
                                        "timeRangeEnd": "18:00:00",
                                        "createdAt": "2026-02-18T09:00:00",
                                        "updatedAt": "2026-02-18T09:00:00"
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
    ApiResponse<MeetingResponse> getMeeting(@PathVariable String code);

    @Operation(
            summary = "모임 생성",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                    @Content(
                                            examples =
                                                    @ExampleObject(
                                                            value =
                                                                    """
                            {
                                "name": "MeetLink 팀 회의",
                                "enableTimeRecommendation": true,
                                "enablePlaceRecommendation": false,
                                "timeAvailabilityType": "WEEKLY",
                                "timeRangeStart": "09:00:00",
                                "timeRangeEnd": "18:00:00"
                            }
                    """))))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                {
                                    "status": true,
                                    "result": {
                                        "id": 1,
                                        "name": "MeetLink 팀 회의",
                                        "code": "A1B2C3",
                                        "enableTimeRecommendation": true,
                                        "enablePlaceRecommendation": false,
                                        "timeAvailabilityType": "WEEKLY",
                                        "timeRangeStart": "09:00:00",
                                        "timeRangeEnd": "18:00:00",
                                        "createdAt": "2026-02-18T09:00:00",
                                        "updatedAt": "2026-02-18T09:00:00"
                                    }
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
                                                "timeRangeStart": "시작 시간(13:00)은 종료 시간(12:00)보다 빨라야 합니다."
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
    ApiResponse<MeetingResponse> createMeeting(@Valid @RequestBody MeetingCreateRequest request);

    @Operation(
            summary = "모임 수정",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                    @Content(
                                            examples =
                                                    @ExampleObject(
                                                            value =
                                                                    """
                            {
                                "name": "테스트"
                            }
                    """))))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                {
                                    "status": true,
                                    "result": {
                                        "id": 1,
                                        "name": "테스트",
                                        "code": "A1B2C3",
                                        "enableTimeRecommendation": true,
                                        "enablePlaceRecommendation": false,
                                        "timeAvailabilityType": "WEEKLY",
                                        "timeRangeStart": "09:00:00",
                                        "timeRangeEnd": "18:00:00",
                                        "createdAt": "2026-02-18T09:00:00",
                                        "updatedAt": "2026-02-18T09:00:00"
                                    }
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
                                                "timeRangeStart": "시작 시간(13:00)은 종료 시간(12:00)보다 빨라야 합니다."
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
                                })),
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
    ApiResponse<MeetingResponse> updateMeeting(
            @PathVariable String code,
            @RequestHeader("X-Participant-Token") String token,
            @Valid @RequestBody MeetingUpdateRequest request);
}
