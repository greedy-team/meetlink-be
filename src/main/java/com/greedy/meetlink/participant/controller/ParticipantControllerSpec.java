package com.greedy.meetlink.participant.controller;

import com.greedy.meetlink.common.ApiResponse;
import com.greedy.meetlink.participant.dto.request.HostTransferRequest;
import com.greedy.meetlink.participant.dto.request.ParticipantJoinRequest;
import com.greedy.meetlink.participant.dto.response.ParticipantJoinResponse;
import com.greedy.meetlink.participant.dto.response.ParticipantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Participant", description = "참여자 API")
public interface ParticipantControllerSpec {
    @Operation(
            summary = "모임 참여",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                    @Content(
                                            examples =
                                                    @ExampleObject(
                                                            value =
                                                                    """
                                            {
                                                "nickname": "테스트"
                                            }
                                            """))))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "참여 성공",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                            {
                                                "status": true,
                                                "result": {
                                                    "token": "1915086f-f1dc-4d32-8311-9ed9b7c38507"
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
                                                "nickname": "닉네임은 필수입니다."
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
                responseCode = "409",
                description = "중복 닉네임",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                            {
                                                "status": false,
                                                "code": "DUPLICATE_NICKNAME",
                                                "message": "이미 사용 중인 닉네임입니다."
                                            }
                                            """)))
    })
    ApiResponse<ParticipantJoinResponse> join(
            @PathVariable String code, @RequestBody ParticipantJoinRequest request);

    @Operation(summary = "참여자 목록 조회")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                examples = {
                                    @ExampleObject(
                                            name = "토큰 있는 경우",
                                            value =
                                                    """
                                            {
                                                "status": true,
                                                "result": [
                                                    {
                                                        "nickname": "테스트1",
                                                        "isPlaceSubmitted": true,
                                                        "isTimeSubmitted": true,
                                                        "isHost": true
                                                    },
                                                    {
                                                        "nickname": "테스트2",
                                                        "isPlaceSubmitted": true,
                                                        "isTimeSubmitted": false,
                                                        "isHost": false
                                                    },
                                                    {
                                                        "nickname": "테스트3",
                                                        "isPlaceSubmitted": false,
                                                        "isTimeSubmitted": false,
                                                        "isHost": false
                                                    }
                                                ]
                                            }
                                            """),
                                    @ExampleObject(
                                            name = "토큰 없는 경우",
                                            value =
                                                    """
                                            {
                                                "status": true,
                                                "result": [
                                                    {
                                                        "nickname": "테스트1",
                                                        "token": "1915086f-f1dc-4d32-8311-9ed9b7c38507",
                                                        "isHost": true
                                                    },
                                                    {
                                                        "nickname": "테스트2",
                                                        "token": "2a3b4c5d-e6f7-8901-abcd-ef1234567890",
                                                        "isHost": false
                                                    },
                                                    {
                                                        "nickname": "테스트3",
                                                        "token": "3c4d5e6f-7890-1234-bcde-f01234567891",
                                                        "isHost": false
                                                    }
                                                ]
                                            }
                                            """)
                                }))
    })
    ApiResponse<List<ParticipantResponse>> getParticipants(
            @PathVariable String code,
            @RequestHeader(value = "X-Participant-Token", required = false) String token);

    @Operation(summary = "내 참여 상태 조회")
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
                                                    "nickname": "테스트1",
                                                    "isPlaceSubmitted": true,
                                                    "isTimeSubmitted": true,
                                                    "isHost": true
                                                }
                                            }
                                            """)))
    })
    ApiResponse<ParticipantResponse> status(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token);

    @Operation(summary = "모임 나가기")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "나가기 성공",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                {
                    "status": true
                }
            """)))
    })
    ApiResponse<Void> leave(
            @PathVariable String code, @RequestHeader("X-Participant-Token") String token);

    @Operation(summary = "모임장 양도")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "양도 성공",
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
                responseCode = "403",
                description = "권한 없음",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                {
                    "status": false,
                    "code": "INSUFFICIENT_PERMISSION",
                    "message": "권한이 없습니다."
                }
            """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "대상 참여자 없음",
                content =
                        @Content(
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                {
                    "status": false,
                    "code": "PARTICIPANT_NOT_FOUND",
                    "message": "참여자를 찾을 수 없습니다."
                }
            """)))
    })
    ApiResponse<Void> transferHost(
            @PathVariable String code,
            @RequestHeader("X-Participant-Token") String token,
            @RequestBody HostTransferRequest request);
}
