package com.greedy.meetlink.config;

import com.greedy.meetlink.common.client.PoiClient;
import com.greedy.meetlink.common.client.TransitClient;
import java.util.Collections;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

// 추후 Mock 구현체 생성 시 해당 클래스 삭제 필요
@TestConfiguration
public class MockClientConfig {
    @Bean
    public PoiClient poiClient() {
        return (latitude, longitude) -> Collections.emptyList();
    }

    @Bean
    public TransitClient transitClient() {
        return (originLat, originLon, destLat, destLon) -> null;
    }
}
