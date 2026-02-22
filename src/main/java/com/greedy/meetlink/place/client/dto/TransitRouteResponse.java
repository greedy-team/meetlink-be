package com.greedy.meetlink.place.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

/**
 * TMap 대중교통 경로 API 응답 POST https://apis.openapi.sk.com/transit/routes
 *
 * <p>응답에서 경로 총 소요 시간(totalTime, 단위: 초)만 추출해서 사용
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransitRouteResponse {

    @JsonProperty("metaData")
    private MetaData metaData;

    /** 응답에서 최단 경로의 소요 시간(분)을 추출 경로가 없거나 오류 시 null 반환 */
    public Double extractMinTravelTimeMinutes() {
        if (metaData == null || metaData.getPlan() == null) {
            return null;
        }
        List<Itinerary> itineraries = metaData.getPlan().getItineraries();
        if (itineraries == null || itineraries.isEmpty()) {
            return null;
        }
        // count=1로 요청하므로 첫 번째 경로 사용
        return itineraries.get(0).getTotalTime() / 60.0;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaData {
        @JsonProperty("plan")
        private Plan plan;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Plan {
        @JsonProperty("itineraries")
        private List<Itinerary> itineraries;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Itinerary {
        @JsonProperty("totalTime")
        private int totalTime; // 단위: 초
    }
}
