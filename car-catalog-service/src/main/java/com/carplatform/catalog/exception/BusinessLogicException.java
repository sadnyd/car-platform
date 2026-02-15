package com.carplatform.catalog.exception;

/**
 * Exception for business logic constraint violations
 */
public class BusinessLogicException extends RuntimeException {
    public BusinessLogicException(String message) {
        super(message);
    }
}
