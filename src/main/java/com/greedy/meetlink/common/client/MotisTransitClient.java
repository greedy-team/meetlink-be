package com.greedy.meetlink.common.client;

import com.greedy.meetlink.common.client.dto.MotisRouteResponse;
import com.greedy.meetlink.common.client.dto.RouteInfo;
import com.greedy.meetlink.common.client.dto.SegmentInfo;
import com.greedy.meetlink.common.util.PolylineDecoder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@Profile("!test")
public class MotisTransitClient implements TransitClient {
    private static final String PLAN_PATH = "/api/v5/plan";
    private static final String WEEKDAY_NOON = "2026-03-09T03:00:00.000Z"; // 월요일 12:00 (KST)

    private final RestClient restClient;

    public MotisTransitClient(
            RestClient.Builder restClientBuilder,
            @Value("${motis.base-url:https://api.transitous.org}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public RouteInfo getPlan(double originLat, double originLon, double destLat, double destLon) {
        String fromPlace = originLat + "," + originLon;
        String toPlace = destLat + "," + destLon;

        try {
            MotisRouteResponse response =
                    restClient
                            .get()
                            .uri(
                                    uriBuilder ->
                                            uriBuilder
                                                    .path(PLAN_PATH)
                                                    .queryParam("fromPlace", fromPlace)
                                                    .queryParam("toPlace", toPlace)
                                                    .queryParam("time", WEEKDAY_NOON)
                                                    .queryParam("timetableView", false)
                                                    .queryParam("maxMatchingDistance", 250)
                                                    .build())
                            .retrieve()
                            .body(MotisRouteResponse.class);

            if (response == null) {
                log.warn(
                        "MOTIS API returned null response: origin=({}, {}), destination=({}, {})",
                        originLat,
                        originLon,
                        destLat,
                        destLon);
                return null;
            }

            MotisRouteResponse.Itinerary itinerary = null;
            if (response.itineraries() != null && !response.itineraries().isEmpty()) {
                itinerary = response.itineraries().getFirst();
            } else if (response.direct() != null && !response.direct().isEmpty()) {
                itinerary = response.direct().getFirst();
            }

            if (itinerary == null) {
                log.warn(
                        "MOTIS no route found: origin=({}, {}), destination=({}, {})",
                        originLat,
                        originLon,
                        destLat,
                        destLon);
                return null;
            }

            double travelTime =
                    itinerary.duration() != null ? itinerary.duration().doubleValue() : 0.0;
            List<SegmentInfo> segments =
                    itinerary.legs() == null
                            ? List.of()
                            : itinerary.legs().stream()
                                    .map(
                                            leg ->
                                                    new SegmentInfo(
                                                            leg.mode(),
                                                            PolylineDecoder.decode(
                                                                    leg.legGeometry() != null
                                                                            ? leg.legGeometry()
                                                                                    .points()
                                                                            : null)))
                                    .toList();

            return new RouteInfo(travelTime, segments);

        } catch (RestClientResponseException e) {
            log.error(
                    "MOTIS API error: status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("MOTIS API call failed unexpectedly: {}", e.getMessage(), e);
            return null;
        }
    }
}
