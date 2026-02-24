package com.greedy.meetlink.client;

import com.greedy.meetlink.client.dto.PoiSearchResponse.PoiPlace;
import com.greedy.meetlink.common.Coordinate;
import java.util.List;

public interface PoiClient {
    List<PoiPlace> searchNearby(Coordinate center);
}
