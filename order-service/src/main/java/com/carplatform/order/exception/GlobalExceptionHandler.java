package com.carplatform.order.exception;

import com.carplatform.order.dto.StandardErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Global Exception Handler for Order Service
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String SERVICE_NAME = "order-service";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<StandardErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        List<StandardErrorResponse.FieldError> fieldErrors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.add(
                        new StandardErrorResponse.FieldError(
                                error.getField(),
                                error.getDefaultMessage())));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new StandardErrorResponse(
                        "VALIDATION_ERROR",
                        "Request validation failed",
                        Instant.now(),
                        SERVICE_NAME,
                        fieldErrors));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<StandardErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new StandardErrorResponse(
                        "RESOURCE_NOT_FOUND",
                        ex.getMessage(),
                        SERVICE_NAME));
    }

    @ExceptionHandler(BusinessLogicException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<StandardErrorResponse> handleBusinessLogicError(BusinessLogicException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new StandardErrorResponse(
                        "BUSINESS_LOGIC_ERROR",
                        ex.getMessage(),
                        SERVICE_NAME));
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<StandardErrorResponse> handleRuntimeError(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new StandardErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred: " + ex.getMessage(),
                        SERVICE_NAME));
    }
}
