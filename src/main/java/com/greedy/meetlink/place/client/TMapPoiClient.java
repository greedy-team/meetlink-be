package com.greedy.meetlink.place.client;

import com.greedy.meetlink.place.Coordinate;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse.PoiPlace;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * TMap POI(관심지점) 검색 클라이언트
 *
 * <p>API: GET https://apis.openapi.sk.com/tmap/pois 문서:
 * https://openapi.sk.com/products/detail?svcSeq=36
 *
 * <p>점수 상위 K개 후보 좌표 주변에서 실제 장소를 검색하는 데 사용 카테고리: 카페(CE7), 음식점(FD6), 편의시설(AD5) 복합 검색
 */
@Slf4j
@Component
@org.springframework.context.annotation.Profile("!test")
public class TMapPoiClient implements PoiClient {

    private static final String POI_SEARCH_PATH = "/tmap/pois";

    // 좌표 주변 탐색 반경 기본값 (단위: m)
    private static final int DEFAULT_SEARCH_RADIUS_METERS = 300;

    // 카테고리별 검색 키워드 (TMap POI 카테고리 코드 기준)
    private static final String SEARCH_CATEGORIES = "카페,음식점,회의실";

    // 좌표당 검색할 최대 POI 수
    private static final int MAX_POI_COUNT = 5;

    private final WebClient webClient;

    public TMapPoiClient(
            WebClient.Builder webClientBuilder, @Value("${tmap.app-key}") String appKey) {
        this.webClient =
                webClientBuilder
                        .baseUrl("https://apis.openapi.sk.com")
                        .defaultHeader("appKey", appKey)
                        .defaultHeader("Accept", "application/json")
                        .build();
    }

    /**
     * 특정 좌표 주변의 실제 장소 목록 조회
     *
     * @param center 탐색 중심 좌표 (점수 상위 후보 좌표)
     * @param radiusMeters 탐색 반경 (미터)
     * @return 실제 장소 목록 (없거나 오류 시 빈 리스트)
     */
    public List<PoiPlace> searchNearby(Coordinate center, int radiusMeters) {
        try {
            PoiSearchResponse response =
                    webClient
                            .get()
                            .uri(
                                    uriBuilder ->
                                            uriBuilder
                                                    .path(POI_SEARCH_PATH)
                                                    .queryParam("version", 1)
                                                    .queryParam("searchKeyword", SEARCH_CATEGORIES)
                                                    .queryParam("centerLat", center.latitude())
                                                    .queryParam("centerLon", center.longitude())
                                                    .queryParam(
                                                            "radius",
                                                            radiusMeters / 1000.0) // API 단위: km
                                                    .queryParam("count", MAX_POI_COUNT)
                                                    .queryParam("searchType", "name")
                                                    .queryParam("format", "json")
                                                    .build())
                            .retrieve()
                            .bodyToMono(PoiSearchResponse.class)
                            .block();

            if (response == null) {
                log.warn("TMap POI 응답 없음: center={}", center);
                return Collections.emptyList();
            }

            List<PoiPlace> places = response.extractPlaces();
            log.debug("TMap POI 검색 완료: center={}, 결과 수={}", center, places.size());
            return places;

        } catch (WebClientResponseException e) {
            log.error(
                    "TMap POI API 오류: status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("TMap POI API 호출 중 예외 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /** 기본 반경(300m)으로 주변 장소 검색 */
    public List<PoiPlace> searchNearby(Coordinate center) {
        return searchNearby(center, DEFAULT_SEARCH_RADIUS_METERS);
    }
}
