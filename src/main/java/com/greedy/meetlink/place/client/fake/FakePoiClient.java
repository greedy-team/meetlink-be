package com.greedy.meetlink.place.client.fake;

import com.greedy.meetlink.place.client.PoiClient;
import com.greedy.meetlink.place.client.dto.PoiSearchResponse.PoiPlace;
import com.greedy.meetlink.place.domain.Coordinate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** 테스트용 가짜 PoiClient 실제 API 호출 없이 고정된 가짜 장소 정보를 반환합니다. 'test' 프로파일에서 활성화됩니다. */
@Slf4j
@Component
@Profile("test")
public class FakePoiClient implements PoiClient {

    @Override
    public List<PoiPlace> searchNearby(Coordinate center) {
        log.info("[FakePoi] 주변 장소 검색 요청: lat={}, lon={}", center.latitude(), center.longitude());

        // 검색된 좌표 바로 그 자리에 "Fake Cafe"가 있다고 가정
        return List.of(
                new PoiPlace(
                        "Fake Cafe at " + String.format("%.4f", center.latitude()),
                        "Fake Address 123",
                        center.latitude(),
                        center.longitude()));
    }
}
