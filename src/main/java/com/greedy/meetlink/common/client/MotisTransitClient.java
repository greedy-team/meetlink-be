package com.greedy.meetlink.common.client;

import com.greedy.meetlink.common.client.dto.MotisRouteResponse;
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

    private final RestClient restClient;

    public MotisTransitClient(
            RestClient.Builder restClientBuilder,
            @Value("${motis.base-url:https://api.transitous.org}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Double getTravelTimeSeconds(
            double originLat, double originLon, double destLat, double destLon) {
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

            Double seconds = null;
            if (response.itineraries() != null && !response.itineraries().isEmpty()) {
                Long d = response.itineraries().get(0).duration();
                if (d != null) seconds = d.doubleValue();
            } else if (response.direct() != null && !response.direct().isEmpty()) {
                Long d = response.direct().get(0).duration();
                if (d != null) seconds = d.doubleValue();
            }

            if (seconds == null) {
                log.warn(
                        "MOTIS no route found: origin=({}, {}), destination=({}, {})",
                        originLat,
                        originLon,
                        destLat,
                        destLon);
            }
            return seconds;

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
