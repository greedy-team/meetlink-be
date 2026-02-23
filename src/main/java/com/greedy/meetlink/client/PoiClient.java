package com.greedy.meetlink.client;

import com.greedy.meetlink.client.dto.PoiSearchResponse.PoiPlace;
import com.greedy.meetlink.common.Coordinate;
import java.util.List;

/** POI(관심지점) 검색 클라이언트 인터페이스 - KakaoPoiClient (Real) - FakePoiClient (Test) */
public interface PoiClient {
    /** 좌표 주변 장소 검색 */
    List<PoiPlace> searchNearby(Coordinate center);
}
