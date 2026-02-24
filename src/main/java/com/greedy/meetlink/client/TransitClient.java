package com.greedy.meetlink.client;

import com.greedy.meetlink.common.Coordinate;

public interface TransitClient {
    Double getTravelTimeSeconds(Coordinate origin, Coordinate destination);
}
