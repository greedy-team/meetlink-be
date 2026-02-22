package com.greedy.meetlink.place.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * TMap POI(관심지점) 검색 API 응답 GET https://apis.openapi.sk.com/tmap/pois
 *
 * <p>카테고리 코드 예시: - 음식점: "FD6" - 카페: "CE7" - 편의시설/회의공간: "AD5"
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PoiSearchResponse {

    @JsonProperty("searchPoiInfo")
    private SearchPoiInfo searchPoiInfo;

    /** 응답에서 실제 장소 목록으로 변환 */
    public List<PoiPlace> extractPlaces() {
        return Optional.ofNullable(searchPoiInfo)
                .map(SearchPoiInfo::getPois)
                .map(Pois::getPoi)
                .orElse(Collections.emptyList())
                .stream()
                .map(
                        poi ->
                                new PoiPlace(
                                        poi.getName(),
                                        poi.getUpperAddrName()
                                                + " "
                                                + poi.getMiddleAddrName()
                                                + " "
                                                + poi.getLowerAddrName(),
                                        Double.parseDouble(poi.getFrontLat()),
                                        Double.parseDouble(poi.getFrontLon())))
                .collect(Collectors.toList());
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchPoiInfo {
        @JsonProperty("pois")
        private Pois pois;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pois {
        @JsonProperty("poi")
        private List<Poi> poi;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Poi {
        @JsonProperty("name")
        private String name;

        @JsonProperty("upperAddrName")
        private String upperAddrName; // 시/도

        @JsonProperty("middleAddrName")
        private String middleAddrName; // 구/군

        @JsonProperty("lowerAddrName")
        private String lowerAddrName; // 동/읍/면

        @JsonProperty("frontLat")
        private String frontLat; // 정문 위도 (문자열로 내려옴)

        @JsonProperty("frontLon")
        private String frontLon; // 정문 경도 (문자열로 내려옴)
    }

    /** 서비스 레이어에서 사용할 장소 정보 */
    public record PoiPlace(String name, String address, double latitude, double longitude) {}
}
