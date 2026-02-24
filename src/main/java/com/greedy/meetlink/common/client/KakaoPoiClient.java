package com.greedy.meetlink.common.client;

import com.greedy.meetlink.common.Coordinate;
import com.greedy.meetlink.common.client.dto.PoiSearchResponse;
import com.greedy.meetlink.common.client.dto.PoiSearchResponse.PoiPlace;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@Profile("!test")
public class KakaoPoiClient implements PoiClient {
    private static final String KEYWORD_SEARCH_PATH = "/v2/local/search/keyword.json";
    private static final int DEFAULT_SEARCH_RADIUS_METERS = 300;
    private static final String SEARCH_QUERY = "카페";
    private static final int MAX_POI_COUNT = 5;

    private final RestClient restClient;

    public KakaoPoiClient(
            RestClient.Builder restClientBuilder,
            @Value("${kakao.api-key}") String apiKey,
            @Value("${kakao.base-url:https://dapi.kakao.com}") String baseUrl) {
        this.restClient =
                restClientBuilder
                        .baseUrl(baseUrl)
                        .defaultHeader("Authorization", "KakaoAK " + apiKey)
                        .build();
    }

    @Override
    public List<PoiPlace> searchNearby(Coordinate center) {
        try {
            PoiSearchResponse response =
                    restClient
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
                            .body(PoiSearchResponse.class);

            if (response == null) {
                log.warn("Kakao POI API returned null response: center={}", center);
                return Collections.emptyList();
            }

            List<PoiPlace> places =
                    response.documents() == null
                            ? Collections.emptyList()
                            : response.documents().stream()
                                    .map(PoiPlace::from)
                                    .collect(Collectors.toList());
            log.debug("Kakao POI search completed: center={}, results={}", center, places.size());
            return places;

        } catch (RestClientResponseException e) {
            log.error(
                    "Kakao POI API error: status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Kakao POI API call failed unexpectedly: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
