package com.greedy.meetlink.place.client;

import com.greedy.meetlink.place.domain.Coordinate;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class TMapTransitClientTest {

    private MockWebServer mockWebServer;
    private TMapTransitClient tMapTransitClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient.Builder webClientBuilder = WebClient.builder();
        tMapTransitClient = new TMapTransitClient(webClientBuilder, baseUrl, "test-app-key");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("정상적인 경로 응답 처리 - 소요 시간 반환")
    void getTravelTimeMinutes_success() throws InterruptedException {
        // given
        Coordinate origin = new Coordinate(37.5665, 126.9780); // 서울시청
        Coordinate destination = new Coordinate(37.5512, 126.9882); // 남산타워

        // totalTime: 1200초 (20분)
        String jsonResponse = """
            {
                "metaData": {
                    "plan": {
                        "itineraries": [
                            {
                                "totalTime": 1200
                            }
                        ]
                    }
                }
            }
        """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // when
        Double result = tMapTransitClient.getTravelTimeMinutes(origin, destination);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(20.0); // 1200초 / 60 = 20분

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/transit/routes");
        assertThat(request.getHeader("appKey")).isEqualTo("test-app-key");
    }

    @Test
    @DisplayName("API 오류 발생 시 null 반환 (500 에러)")
    void getTravelTimeMinutes_serverError() {
        // given
        Coordinate origin = new Coordinate(37.5665, 126.9780);
        Coordinate destination = new Coordinate(37.5512, 126.9882);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // when
        Double result = tMapTransitClient.getTravelTimeMinutes(origin, destination);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("응답이 비어있거나 형식이 잘못된 경우 null 반환")
    void getTravelTimeMinutes_invalidResponse() {
        // given
        Coordinate origin = new Coordinate(37.5665, 126.9780);
        Coordinate destination = new Coordinate(37.5512, 126.9882);

        // 비어있는 JSON -> extractMinTravelTimeMinutes()에서 null 반환 예상
        mockWebServer.enqueue(new MockResponse()
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));

        // when
        Double result = tMapTransitClient.getTravelTimeMinutes(origin, destination);

        // then
        assertThat(result).isNull();
    }
}
