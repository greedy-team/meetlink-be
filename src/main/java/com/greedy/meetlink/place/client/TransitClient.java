package com.greedy.meetlink.place.client;

import com.greedy.meetlink.place.domain.Coordinate;

/** 대중교통 경로 조회 클라이언트 인터페이스 - TMapTransitClient (Real) - FakeTransitClient (Test) */
public interface TransitClient {
    /** 출발지 → 목적지 대중교통 이동 시간(분) 조회 */
    Double getTravelTimeMinutes(Coordinate origin, Coordinate destination);
}
