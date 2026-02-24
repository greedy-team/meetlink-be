package com.greedy.meetlink.client;

import com.greedy.meetlink.client.dto.MotisRouteResponse;
import com.greedy.meetlink.common.Coordinate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** MOTIS 대중교통 경로 클라이언트 (GET /api/v5/plan) */
@Slf4j
@Component
public class MotisTransitClient implements TransitClient {
    private static final String PLAN_PATH = "/api/v5/plan";

    private final RestClient restClient;

    public MotisTransitClient(
            RestClient.Builder restClientBuilder,
            @Value("${motis.base-url:https://api.transitous.org}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Double getTravelTimeSeconds(Coordinate origin, Coordinate destination) {
        String fromPlace = origin.latitude() + "," + origin.longitude();
        String toPlace = destination.latitude() + "," + destination.longitude();

        try {
            MotisRouteResponse response =
                    restClient
                            .get()
                            .uri(
                                    uriBuilder ->
                                            uriBuilder
                                                    .path(PLAN_PATH)
                                                    .queryParam("fromPlace", fromPlace)
                                                    .queryParam("toPlace", toPlace)
                                                    .build())
                            .retrieve()
                            .body(MotisRouteResponse.class);

            if (response == null) {
                log.warn("MOTIS API 응답 없음: origin={}, destination={}", origin, destination);
                return null;
            }

            Double seconds = response.extractMinTravelTimeSeconds();
            if (seconds == null) {
                log.warn("MOTIS 경로 없음: origin={}, destination={}", origin, destination);
            }
            return seconds;

        } catch (RestClientResponseException e) {
            log.error(
                    "MOTIS API 오류: status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("MOTIS API 호출 중 예외 발생: {}", e.getMessage(), e);
            return null;
        }
    }
}
