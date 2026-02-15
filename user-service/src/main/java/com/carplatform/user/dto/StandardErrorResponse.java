package com.carplatform.user.dto;

import java.time.Instant;
import java.util.List;

/**
 * Standard Error Response DTO
 */
public record StandardErrorResponse(
        String errorCode,
        String message,
        Instant timestamp,
        String serviceName,
        List<FieldError> fieldErrors) {

    public StandardErrorResponse(String errorCode, String message, String serviceName) {
        this(errorCode, message, Instant.now(), serviceName, null);
    }

    public record FieldError(String fieldName, String message) {
    }
}
