package com.greedy.meetlink.common.client;

import com.greedy.meetlink.common.client.dto.RouteInfo;

public interface TransitClient {
    RouteInfo getPlan(double originLat, double originLon, double destLat, double destLon);

    default Double getTravelTime(
            double originLat, double originLon, double destLat, double destLon) {
        RouteInfo plan = getPlan(originLat, originLon, destLat, destLon);
        return plan != null ? plan.travelTime() : null;
    }
}
