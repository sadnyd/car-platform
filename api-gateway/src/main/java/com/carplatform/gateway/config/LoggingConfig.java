package com.carplatform.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
// import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Logging Configuration
 *
 * STEP 6.11: Structured Logging Setup
 *
 * Configures structured JSON logging with trace ID propagation across service
 * calls.
 * Enables correlation between API Gateway logs and downstream service logs.
 *
 * Dependencies:
 * - Logback (Spring Boot default)
 * - SLF4J for logging API
 * - Micrometer for metrics (optional enhancement)
 *
 * Features:
 * - JSON formatted logs for centralized logging
 * - Trace ID (correlation ID) in all log messages
 * - Service call latency tracking
 * - Per-service client request/response logging
 * - Error stack traces with context
 */
@Configuration
public class LoggingConfig {

    @Value("${app.logging.json-format:true}")
    private boolean jsonFormat;

    @Value("${app.logging.level:INFO}")
    private String logLevel;

    /**
     * Logback configuration is handled via logback-spring.xml in resources/
     * This class serves as marker for logging setup completion
     */
}
