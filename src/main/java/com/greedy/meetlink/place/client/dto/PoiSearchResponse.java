package com.greedy.meetlink.place.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * 카카오 키워드 장소 검색 API 응답 GET https://dapi.kakao.com/v2/local/search/keyword.json
 *
 * <p>x: 경도(longitude), y: 위도(latitude)
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PoiSearchResponse {

    @JsonProperty("documents")
    private List<Document> documents;

    public List<PoiPlace> extractPlaces() {
        return Optional.ofNullable(documents).orElse(Collections.emptyList()).stream()
                .map(
                        doc ->
                                new PoiPlace(
                                        doc.getPlaceName(),
                                        doc.getPreferredAddress(),
                                        Double.parseDouble(doc.getY()),
                                        Double.parseDouble(doc.getX())))
                .collect(Collectors.toList());
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        @JsonProperty("place_name")
        private String placeName;

        @JsonProperty("road_address_name")
        private String roadAddressName;

        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("x") // 경도(longitude)
        private String x;

        @JsonProperty("y") // 위도(latitude)
        private String y;

        /** 도로명주소 우선, 없으면 지번주소 */
        public String getPreferredAddress() {
            return (roadAddressName != null && !roadAddressName.isBlank())
                    ? roadAddressName
                    : addressName;
        }
    }

    /** 서비스 레이어에서 사용할 장소 정보 */
    public record PoiPlace(String name, String address, double latitude, double longitude) {}
}
