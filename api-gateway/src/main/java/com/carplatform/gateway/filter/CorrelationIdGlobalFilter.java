package com.carplatform.gateway.filter;

import com.carplatform.gateway.util.TraceIdManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
// import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * Correlation ID Global Filter
 *
 * STEP 6.11: Trace Correlation Filter
 *
 * Global filter for Spring Cloud Gateway that:
 * 1. Extracts trace ID from request header (or generates new one)
 * 2. Stores in MDC for all downstream logging
 * 3. Adds trace ID to downstream service requests
 * 4. Logs request start/end with latency
 *
 * Execution Order: Highest priority (runs first)
 *
 * Example Log Output:
 * [correlation_id=550e8400-e29b-41d4-a716-446655440000] GET /api/cars/listing
 * duration=145ms status=200
 */
@Slf4j
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().toString();

        // Extract trace ID from request header (if exists) or generate new one
        String traceIdFromHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(TraceIdManager.getHeaderName());

        String traceId = TraceIdManager.initialize(traceIdFromHeader);

        log.info("Aggregation Pipeline - Request Start: {} {} [traceId={}]", method, path, traceId);

        // Add trace ID to response headers
        exchange.getResponse().getHeaders().add(TraceIdManager.getHeaderName(), traceId);

        // Add trace ID to downstream requests (via context)
        return chain.filter(exchange)
                .contextWrite(Context.of(TraceIdManager.getKey(), traceId))
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 500;

                    log.info(
                            "Aggregation Pipeline - Request End: {} {} status={} duration={}ms [traceId={}]",
                            method,
                            path,
                            statusCode,
                            duration,
                            traceId);

                    TraceIdManager.clear();
                });
    }

    /**
     * Filter order: HIGHEST_PRECEDENCE = -2147483648
     * This ensures correlation ID is set up before any other filters
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
