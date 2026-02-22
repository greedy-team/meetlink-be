package com.greedy.meetlink.place.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * TMap 대중교통 경로 API 요청 Body POST https://apis.openapi.sk.com/transit/routes
 *
 * <p>TMap 좌표 스펙: X = 경도(longitude), Y = 위도(latitude)
 */
@Builder
public record TransitRouteRequest(
        @JsonProperty("startX") String startX, // 출발지 경도
        @JsonProperty("startY") String startY, // 출발지 위도
        @JsonProperty("endX") String endX, // 목적지 경도
        @JsonProperty("endY") String endY, // 목적지 위도
        @JsonProperty("count") int count, // 결과 경로 수 (1~10)
        @JsonProperty("lang") int lang, // 언어 (0: 한국어)
        @JsonProperty("format") String format // 응답 형식 (json)
        ) {
    /** 출발지 → 목적지 기본 요청 생성 (경로 1개, 한국어, JSON) */
    public static TransitRouteRequest of(
            double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        return TransitRouteRequest.builder()
                .startX(String.valueOf(startLongitude))
                .startY(String.valueOf(startLatitude))
                .endX(String.valueOf(endLongitude))
                .endY(String.valueOf(endLatitude))
                .count(1)
                .lang(0)
                .format("json")
                .build();
    }
}
