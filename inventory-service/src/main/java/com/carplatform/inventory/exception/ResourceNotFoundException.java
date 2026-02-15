package com.carplatform.inventory.exception;

/**
 * Exception for resource not found scenarios
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
