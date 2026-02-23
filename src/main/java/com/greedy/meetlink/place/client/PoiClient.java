package com.greedy.meetlink.place.client;

import com.greedy.meetlink.place.Coordinate;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse.PoiPlace;
import java.util.List;

/** POI(관심지점) 검색 클라이언트 인터페이스 - KakaoPoiClient (Real) - FakePoiClient (Test) */
public interface PoiClient {
    /** 좌표 주변 장소 검색 */
    List<PoiPlace> searchNearby(Coordinate center);
}
