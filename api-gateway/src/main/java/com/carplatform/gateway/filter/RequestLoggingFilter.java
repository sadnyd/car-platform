package com.carplatform.gateway.filter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        log.info("Incoming Request → {} {}", method, path);

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;

                    int status = Optional.ofNullable(exchange.getResponse())
                            .map(response -> response.getStatusCode())
                            .map(statusCode -> statusCode.value())
                            .orElse(0);
                    log.info("Outgoing Response ← {} {} | Status: {} | Time: {} ms",
                            method, path, status, duration);
                }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
