package com.carplatform.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a downstream service is unavailable (HTTP 503)
 * 
 * 
 * Used by:
 * - InventoryServiceClient: When inventory service cannot be reached
 * - CatalogServiceClient: When catalog service cannot be reached
 * - AggregationService: When service call fails after retries
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends RuntimeException {

    private String serviceName;
    private int retryAttempts;

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, String serviceName) {
        super(message);
        this.serviceName = serviceName;
    }

    public ServiceUnavailableException(String message, String serviceName, int retryAttempts) {
        super(message);
        this.serviceName = serviceName;
        this.retryAttempts = retryAttempts;
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }
}
