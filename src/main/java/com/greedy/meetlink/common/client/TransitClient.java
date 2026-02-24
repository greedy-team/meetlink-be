package com.greedy.meetlink.common.client;

import com.greedy.meetlink.common.Coordinate;

public interface TransitClient {
    Double getTravelTimeSeconds(Coordinate origin, Coordinate destination);
}
