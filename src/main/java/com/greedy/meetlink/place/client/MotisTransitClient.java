package com.greedy.meetlink.place.client;

import com.greedy.meetlink.place.Coordinate;
import com.greedy.meetlink.place.client.dto.MotisRouteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/** MOTIS 대중교통 경로 클라이언트 (GET /api/v5/plan) */
@Slf4j
@Component
@Profile("!test")
public class MotisTransitClient implements TransitClient {

    private static final String PLAN_PATH = "/api/v5/plan";

    private final WebClient webClient;

    public MotisTransitClient(
            WebClient.Builder webClientBuilder,
            @Value("${motis.base-url:https://api.transitous.org}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Double getTravelTimeSeconds(Coordinate origin, Coordinate destination) {
        String fromPlace = origin.latitude() + "," + origin.longitude();
        String toPlace = destination.latitude() + "," + destination.longitude();

        try {
            MotisRouteResponse response =
                    webClient
                            .get()
                            .uri(
                                    uriBuilder ->
                                            uriBuilder
                                                    .path(PLAN_PATH)
                                                    .queryParam("fromPlace", fromPlace)
                                                    .queryParam("toPlace", toPlace)
                                                    .build())
                            .retrieve()
                            .bodyToMono(MotisRouteResponse.class)
                            .block();

            if (response == null) {
                log.warn("MOTIS API 응답 없음: origin={}, destination={}", origin, destination);
                return null;
            }

            Double seconds = response.extractMinTravelTimeSeconds();
            if (seconds == null) {
                log.warn("MOTIS 경로 없음: origin={}, destination={}", origin, destination);
            }
            return seconds;

        } catch (WebClientResponseException e) {
            log.error("MOTIS API 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("MOTIS API 호출 중 예외 발생: {}", e.getMessage(), e);
            return null;
        }
    }
}
