package com.greedy.meetlink.common.client;

import com.greedy.meetlink.common.Coordinate;
import com.greedy.meetlink.common.client.dto.PoiSearchResponse.PoiPlace;
import java.util.List;

public interface PoiClient {
    List<PoiPlace> searchNearby(Coordinate center);
}
