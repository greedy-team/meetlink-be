package com.greedy.meetlink.place.domain;

import com.greedy.meetlink.place.client.dto.PoiSearchResponse.PoiPlace;

public record PlaceSearchResult(
    String name,
    String address,
    Coordinate coordinate
) {
    public static PlaceSearchResult from(PoiPlace poiPlace) {
        return new PlaceSearchResult(
            poiPlace.name(),
            poiPlace.address(),
            new Coordinate(poiPlace.latitude(), poiPlace.longitude())
        );
    }

    // TMap POI에는 ID가 없으므로 이름과 주소 조합으로 식별자 생성 (중복 제거용)
    public String uniqueId() {
        return name + "|" + address;
    }
}
