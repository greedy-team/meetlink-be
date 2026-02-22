package com.greedy.meetlink.place.client;

import com.greedy.meetlink.place.Coordinate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** 테스트용 가짜 TransitClient API 호출 없이 직선 거리에 비례하여 이동 시간을 계산합니다. 'test' 프로파일에서 활성화됩니다. */
@Slf4j
@Component
@Profile("test")
public class FakeTransitClient implements TransitClient {

    @Override
    public Double getTravelTimeMinutes(Coordinate origin, Coordinate destination) {
        double distanceKm = origin.distanceTo(destination);
        double timeMinutes = (distanceKm * 2.0) + 5.0;

        log.info(
                "[FakeTransit] {} -> {}: 거리={:.2f}km, 시간={:.1f}분",
                origin,
                destination,
                distanceKm,
                timeMinutes);

        return timeMinutes;
    }
}
