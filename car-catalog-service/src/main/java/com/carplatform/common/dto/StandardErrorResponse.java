package com.carplatform.common.dto;

import java.time.Instant;
import java.util.List;

/**
 * Standard Error Response DTO
 * 
 * Used across all services for consistent error handling.
 * Includes error code, message, timestamp, and optional field validation
 * errors.
 */
public record StandardErrorResponse(
        String errorCode,
        String message,
        Instant timestamp,
        String serviceName,
        List<FieldError> fieldErrors) {

    /**
     * Convenience constructor with minimal fields
     */
    public StandardErrorResponse(String errorCode, String message, String serviceName) {
        this(errorCode, message, Instant.now(), serviceName, null);
    }

    /**
     * Field validation error details
     */
    public record FieldError(String fieldName, String message) {
    }
}
