package com.carplatform.order.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateOrderRequest Validation Tests")
class CreateOrderRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationForValidRequest() {
        CreateOrderRequest request = new CreateOrderRequest(UUID.randomUUID(), UUID.randomUUID(), 30);

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenCarIdIsNull() {
        CreateOrderRequest request = new CreateOrderRequest(null, UUID.randomUUID(), 30);

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("carId", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFailWhenReservationExpiryBelowMinimum() {
        CreateOrderRequest request = new CreateOrderRequest(UUID.randomUUID(), UUID.randomUUID(), 0);

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> "reservationExpiryMinutes".equals(v.getPropertyPath().toString())));
    }

    @Test
    void shouldFailWhenReservationExpiryAboveMaximum() {
        CreateOrderRequest request = new CreateOrderRequest(UUID.randomUUID(), UUID.randomUUID(), 2000);

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> "reservationExpiryMinutes".equals(v.getPropertyPath().toString())));
    }
}
