package com.carplatform.order.config;

// import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
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

    @Bean
    public WebClient webClient() {
        // Configure Netty HTTP client with timeouts
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5 second connection timeout
                .option(ChannelOption.SO_KEEPALIVE, true)
                .responseTimeout(java.time.Duration.ofSeconds(5)) // 5 second response timeout
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
