package com.greedy.meetlink.common.client;

public interface TransitClient {
    Double getTravelTimeSeconds(double originLat, double originLon, double destLat, double destLon);
}
