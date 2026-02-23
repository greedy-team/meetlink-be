package com.greedy.meetlink.place.client;

import com.greedy.meetlink.place.Coordinate;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse.PoiPlace;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/** 카카오 키워드 장소 검색 클라이언트 (GET /v2/local/search/keyword.json) */
@Slf4j
@Component
@Profile("!test")
public class KakaoPoiClient implements PoiClient {

    private static final String KEYWORD_SEARCH_PATH = "/v2/local/search/keyword.json";
    private static final int DEFAULT_SEARCH_RADIUS_METERS = 300;
    private static final String SEARCH_QUERY = "카페";
    private static final int MAX_POI_COUNT = 5;

    private final WebClient webClient;

    public KakaoPoiClient(
            WebClient.Builder webClientBuilder,
            @Value("${kakao.api-key}") String restApiKey,
            @Value("${kakao.base-url:https://dapi.kakao.com}") String baseUrl) {
        this.webClient =
                webClientBuilder
                        .baseUrl(baseUrl)
                        .defaultHeader("Authorization", "KakaoAK " + restApiKey)
                        .build();
    }

    @Override
    public List<PoiPlace> searchNearby(Coordinate center) {
        try {
            PoiSearchResponse response =
                    webClient
                            .get()
                            .uri(
                                    uriBuilder ->
                                            uriBuilder
                                                    .path(KEYWORD_SEARCH_PATH)
                                                    .queryParam("query", SEARCH_QUERY)
                                                    .queryParam("x", center.longitude())
                                                    .queryParam("y", center.latitude())
                                                    .queryParam(
                                                            "radius", DEFAULT_SEARCH_RADIUS_METERS)
                                                    .queryParam("sort", "distance")
                                                    .queryParam("size", MAX_POI_COUNT)
                                                    .build())
                            .retrieve()
                            .bodyToMono(PoiSearchResponse.class)
                            .block();

            if (response == null) {
                log.warn("Kakao POI 응답 없음: center={}", center);
                return Collections.emptyList();
            }

            List<PoiPlace> places = response.extractPlaces();
            log.debug("Kakao POI 검색 완료: center={}, 결과 수={}", center, places.size());
            return places;

        } catch (WebClientResponseException e) {
            log.error(
                    "Kakao POI API 오류: status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Kakao POI API 호출 중 예외 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
