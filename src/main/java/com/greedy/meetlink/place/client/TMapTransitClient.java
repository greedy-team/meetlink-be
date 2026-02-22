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
 * <p>API: POST https://apis.openapi.sk.com/transit/routes 인증: Header appKey 사용
 *
 * <p>appKey는 application.yml에 tmap.app-key 로 설정
 */
@Slf4j
@Component
@org.springframework.context.annotation.Profile("!test")
public class TMapTransitClient implements TransitClient {

    private static final String TRANSIT_ROUTES_PATH = "/transit/routes";

    private final WebClient webClient;

    public TMapTransitClient(
            WebClient.Builder webClientBuilder,
            @Value("${tmap.base-url:https://apis.openapi.sk.com}") String baseUrl,
            @Value("${tmap.app-key}") String appKey) {
        this.webClient =
                webClientBuilder
                        .baseUrl(baseUrl)
                        .defaultHeader("appKey", appKey)
                        .defaultHeader("Accept", "application/json")
                        .defaultHeader("Content-Type", "application/json")
                        .build();
    }

    /**
     * 출발지 → 목적지 대중교통 이동 시간(분) 조회
     *
     * @param origin 출발지 좌표
     * @param destination 목적지 좌표
     * @return 이동 시간(분), 경로 없거나 오류 시 null
     */
    public Double getTravelTimeMinutes(Coordinate origin, Coordinate destination) {
        TransitRouteRequest request =
                TransitRouteRequest.of(
                        origin.latitude(), origin.longitude(),
                        destination.latitude(), destination.longitude());

        int maxRetries = 3;
        long retryDelayMs = 2000;

        for (int i = 0; i <= maxRetries; i++) {
            try {
                TransitRouteResponse response =
                        webClient
                                .post()
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
                if (e.getStatusCode().value() == 429) {
                    if (i < maxRetries) {
                        log.warn(
                                "TMap API 429 오류 (시도 {}/{}). {}ms 후 재시도...",
                                i + 1,
                                maxRetries + 1,
                                retryDelayMs);
                        try {
                            Thread.sleep(retryDelayMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        retryDelayMs *= 2; // 지수 백오프
                        continue;
                    }
                }
                log.error(
                        "TMap API 오류: status={}, body={}",
                        e.getStatusCode(),
                        e.getResponseBodyAsString());
                return null;
            } catch (Exception e) {
                log.error("TMap API 호출 중 예외 발생: {}", e.getMessage(), e);
                return null;
            }
        }
        return null;
    }
}
