package com.greedy.meetlink.place.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * WebClient 공통 설정
 *
 * <p>TMapTransitClient 등에서 WebClient.Builder를 주입받아 사용 기본 타임아웃 설정 적용 (5초)
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient =
                HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                        .responseTimeout(Duration.ofMillis(5000))
                        .doOnConnected(
                                conn ->
                                        conn.addHandlerLast(
                                                        new ReadTimeoutHandler(
                                                                5000, TimeUnit.MILLISECONDS))
                                                .addHandlerLast(
                                                        new WriteTimeoutHandler(
                                                                5000, TimeUnit.MILLISECONDS)));

        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
