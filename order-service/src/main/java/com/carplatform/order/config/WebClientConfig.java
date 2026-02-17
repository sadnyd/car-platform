package com.carplatform.order.config;

// import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.slf4j.MDC;
import reactor.netty.http.client.HttpClient;
// import reactor.netty.tcp.TcpClient;

import java.util.concurrent.TimeUnit;

/**
 * Centralized WebClient Configuration
 * 
 * Purpose:
 * - Provides a shared WebClient bean for inter-service communication
 * - Configures timeouts, connection pooling, and resilience
 * - Supports both Inventory and Catalog service calls
 * 
 * Strategy:
 * - Non-blocking HTTP client (WebClient over deprecated RestTemplate)
 * - Reactive patterns for future scalability
 * - Configurable timeouts per service (handled at call site)
 */
@Configuration
public class WebClientConfig {

    private static final String TRACE_HEADER = "X-Trace-Id";
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Bean
    public WebClient webClient() {
        // Configure Netty HTTP client with timeouts
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .responseTimeout(java.time.Duration.ofSeconds(3))
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(3, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(3, TimeUnit.SECONDS)));

        ExchangeFilterFunction correlationFilter = (request, next) -> {
            String correlationId = MDC.get("correlation_id");
            if (correlationId == null || correlationId.isBlank()) {
                return next.exchange(request);
            }

            return next.exchange(
                    org.springframework.web.reactive.function.client.ClientRequest.from(request)
                            .headers(headers -> {
                                headers.set(TRACE_HEADER, correlationId);
                                headers.set(CORRELATION_HEADER, correlationId);
                                if (!headers.containsKey(HttpHeaders.ACCEPT)) {
                                    headers.set(HttpHeaders.ACCEPT, "application/json");
                                }
                            })
                            .build());
        };

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(correlationFilter)
                .build();
    }
}
