package com.greedy.meetlink.result.controller;

import com.greedy.meetlink.common.ApiResponse;
import com.greedy.meetlink.result.dto.response.MeetingResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "MeetingResult", description = "모임 추천 결과 API")
public interface MeetingResultControllerSpec {
    @Operation(summary = "추천 결과 조회")
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
                                            "placeCandidate": {
                                                "address": "서울 서초구 동산로 3",
                                                "avgTravelTime": 2760.0,
                                                "id": 22,
                                                "latitude": 37.46906940866943,
                                                "longitude": 127.04028626682705,
                                                "maxTravelTime": 2880.0,
                                                "name": "메가MGC커피 양재시민의숲점",
                                                "rank": 1
                                            },
                                            "timeCandidate": {
                                                "availableCount": 2,
                                                "date": "2026-02-21",
                                                "dayOfWeek": null,
                                                "endTime": "10:00:00",
                                                "id": 3,
                                                "rank": 1,
                                                "startTime": "09:00:00"
                                            },
                                            "createdAt": "2026-02-24T16:34:15.938135",
                                            "updatedAt": "2026-02-24T21:18:03.931451"
                                        }
                                    }
                                    """)))
    })
    ApiResponse<MeetingResultResponse> get(@PathVariable String code);
}
