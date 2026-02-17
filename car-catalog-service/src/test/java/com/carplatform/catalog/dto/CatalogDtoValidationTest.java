package com.carplatform.catalog.dto;

import com.carplatform.catalog.model.CarStatus;
import com.carplatform.catalog.model.FuelType;
import com.carplatform.catalog.model.TransmissionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Catalog DTO Validation Tests")
class CatalogDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createRequestShouldFailWhenBrandBlank() {
        CreateCarRequest request = new CreateCarRequest(
                " ",
                "Corolla",
                "XLE",
                2024,
                FuelType.PETROL,
                TransmissionType.AUTOMATIC,
                BigDecimal.valueOf(25000),
                "desc");

        Set<ConstraintViolation<CreateCarRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "brand".equals(v.getPropertyPath().toString())));
    }

    @Test
    void createRequestShouldFailWhenPriceTooLow() {
        CreateCarRequest request = new CreateCarRequest(
                "Toyota",
                "Corolla",
                "XLE",
                2024,
                FuelType.PETROL,
                TransmissionType.AUTOMATIC,
                BigDecimal.ZERO,
                "desc");

        Set<ConstraintViolation<CreateCarRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "price".equals(v.getPropertyPath().toString())));
    }

    @Test
    void updateRequestShouldFailWhenStatusNull() {
        UpdateCarRequest request = new UpdateCarRequest(
                "Toyota",
                "Corolla",
                "XSE",
                2025,
                BigDecimal.valueOf(26000),
                "desc",
                null);

        Set<ConstraintViolation<UpdateCarRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "status".equals(v.getPropertyPath().toString())));
    }

    @Test
    void updateRequestShouldPassWithValidStatus() {
        UpdateCarRequest request = new UpdateCarRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                CarStatus.ACTIVE);

        Set<ConstraintViolation<UpdateCarRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
