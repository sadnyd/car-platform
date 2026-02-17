package com.carplatform.order.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String TRACE_HEADER = "X-Trace-Id";
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = Optional.ofNullable(request.getHeader(TRACE_HEADER))
                .filter(value -> !value.isBlank())
                .or(() -> Optional.ofNullable(request.getHeader(CORRELATION_HEADER)).filter(value -> !value.isBlank()))
                .orElseGet(() -> UUID.randomUUID().toString());

        String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        MDC.put("service", applicationName);
        MDC.put("correlation_id", correlationId);
        MDC.put("trace_id", correlationId);
        MDC.put("span_id", spanId);

        response.setHeader(TRACE_HEADER, correlationId);
        response.setHeader(CORRELATION_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
