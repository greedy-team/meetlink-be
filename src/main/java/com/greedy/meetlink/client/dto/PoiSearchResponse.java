package com.greedy.meetlink.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PoiSearchResponse(@JsonProperty("documents") List<Document> documents) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
            @JsonProperty("place_name") String placeName,
            @JsonProperty("road_address_name") String roadAddressName,
            @JsonProperty("address_name") String addressName,
            String x, // 경도
            String y // 위도
            ) {

        /** 도로명주소 우선, 없으면 지번주소 */
        public String preferredAddress() {
            return (roadAddressName != null && !roadAddressName.isBlank())
                    ? roadAddressName
                    : addressName;
        }
    }

    public record PoiPlace(String name, String address, double latitude, double longitude) {
        public static PoiPlace from(Document doc) {
            return new PoiPlace(
                    doc.placeName(),
                    doc.preferredAddress(),
                    Double.parseDouble(doc.y()),
                    Double.parseDouble(doc.x()));
        }
    }
}
