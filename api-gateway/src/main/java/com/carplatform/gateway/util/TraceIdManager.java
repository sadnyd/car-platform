package com.carplatform.gateway.util;

import org.slf4j.MDC;
import java.util.UUID;

/**
 * Trace ID Manager
 *
 * Manages trace IDs (correlation IDs) across all service calls in the
 * aggregation pipeline.
 * Uses SLF4J MDC (Mapped Diagnostic Context) to propagate correlation ID.
 *
 * Usage:
 * - At request entry: TraceIdManager.initialize() → generates/stores trace ID
 * - During service calls: TraceIdManager.get() → returns current trace ID
 * - At request end: TraceIdManager.clear() → cleans up MDC
 *
 * Features:
 * - Automatic trace ID generation (UUID format)
 * - Thread-safe MDC usage
 * - Fallback support for pre-existing trace IDs
 * - Centralized correlation ID access
 */
public class TraceIdManager {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String CORRELATION_ID = "correlation_id";

    /**
     * Initialize trace ID from header or generate new one
     *
     * @param traceIdFromRequest Optional trace ID from request header
     * @return Generated or provided trace ID
     */
    public static String initialize(String traceIdFromRequest) {
        String traceId = traceIdFromRequest != null && !traceIdFromRequest.isEmpty()
                ? traceIdFromRequest
                : UUID.randomUUID().toString();

        MDC.put(CORRELATION_ID, traceId);
        return traceId;
    }

    /**
     * Initialize with automatic UUID generation
     *
     * @return Generated trace ID
     */
    public static String initialize() {
        return initialize(null);
    }

    /**
     * Get current trace ID from MDC
     *
     * @return Current trace ID or empty string if not set
     */
    public static String get() {
        String traceId = MDC.get(CORRELATION_ID);
        return traceId != null ? traceId : "";
    }

    /**
     * Clear trace ID from MDC (call at end of request)
     */
    public static void clear() {
        MDC.remove(CORRELATION_ID);
    }

    /**
     * Get MDC context key name
     *
     * @return MDC key for correlation ID
     */
    public static String getKey() {
        return CORRELATION_ID;
    }

    /**
     * Get HTTP header name for trace ID
     *
     * @return HTTP header name
     */
    public static String getHeaderName() {
        return TRACE_ID_HEADER;
    }
}
