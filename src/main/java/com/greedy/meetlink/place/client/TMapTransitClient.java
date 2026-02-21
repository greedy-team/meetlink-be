package com.greedy.meetlink.place.client;

import com.greedy.meetlink.place.client.dto.TransitRouteRequest;
import com.greedy.meetlink.place.client.dto.TransitRouteResponse;
import com.greedy.meetlink.place.domain.Coordinate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * SK Open API - TMap 대중교통 경로 클라이언트
 *
 * API: POST https://apis.openapi.sk.com/transit/routes
 * 인증: Header appKey 사용
 *
 * appKey는 application.yml에 tmap.app-key 로 설정
 */
@Slf4j
@Component
public class TMapTransitClient {

    private static final String TRANSIT_ROUTES_PATH = "/transit/routes";

    private final WebClient webClient;

    public TMapTransitClient(
            WebClient.Builder webClientBuilder,
            @Value("${tmap.app-key}") String appKey) {
        this.webClient = webClientBuilder
                .baseUrl("https://apis.openapi.sk.com")
                .defaultHeader("appKey", appKey)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * 출발지 → 목적지 대중교통 이동 시간(분) 조회
     *
     * @param origin      출발지 좌표
     * @param destination 목적지 좌표
     * @return 이동 시간(분), 경로 없거나 오류 시 null
     */
    public Double getTravelTimeMinutes(Coordinate origin, Coordinate destination) {
        TransitRouteRequest request = TransitRouteRequest.of(
                origin.latitude(), origin.longitude(),
                destination.latitude(), destination.longitude()
        );

        try {
            TransitRouteResponse response = webClient.post()
                    .uri(TRANSIT_ROUTES_PATH)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TransitRouteResponse.class)
                    .block();

            if (response == null) {
                log.warn("TMap API 응답 없음: origin={}, destination={}", origin, destination);
                return null;
            }

            return response.extractMinTravelTimeMinutes();

        } catch (WebClientResponseException e) {
            log.error("TMap API 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("TMap API 호출 중 예외 발생: {}", e.getMessage(), e);
            return null;
        }
    }
}
