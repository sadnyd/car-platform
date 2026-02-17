package com.carplatform.inventory.dto;

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

@DisplayName("Inventory DTO Validation Tests")
class InventoryDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createInventoryRequestShouldFailWhenLocationBlank() {
        CreateInventoryRequest request = new CreateInventoryRequest(UUID.randomUUID(), 1, " ");

        Set<ConstraintViolation<CreateInventoryRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "location".equals(v.getPropertyPath().toString())));
    }

    @Test
    void createInventoryRequestShouldFailWhenAvailableUnitsNegative() {
        CreateInventoryRequest request = new CreateInventoryRequest(UUID.randomUUID(), -1, "warehouse-a");

        Set<ConstraintViolation<CreateInventoryRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "availableUnits".equals(v.getPropertyPath().toString())));
    }

    @Test
    void reserveInventoryRequestShouldFailWhenUnitsLessThanOne() {
        ReserveInventoryRequest request = new ReserveInventoryRequest(UUID.randomUUID(), 0);

        Set<ConstraintViolation<ReserveInventoryRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "units".equals(v.getPropertyPath().toString())));
    }

    @Test
    void updateInventoryRequestShouldPassForValidPayload() {
        UpdateInventoryRequest request = new UpdateInventoryRequest(UUID.randomUUID(), 5, "warehouse-b");

        Set<ConstraintViolation<UpdateInventoryRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
