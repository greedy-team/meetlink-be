package com.greedy.meetlink.client;

import com.greedy.meetlink.common.Coordinate;

/** 대중교통 경로 조회 클라이언트 인터페이스 - MotisTransitClient (Real) - FakeTransitClient (Test) */
public interface TransitClient {
    /** 출발지 → 목적지 대중교통 이동 시간(초) 조회 */
    Double getTravelTimeSeconds(Coordinate origin, Coordinate destination);
}
