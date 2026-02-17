package com.carplatform.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User DTO Validation Tests")
class UserDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void registerRequestShouldFailWhenNameBlank() {
        RegisterUserRequest request = new RegisterUserRequest(" ", "alice@example.com", "+14155552671");

        Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "name".equals(v.getPropertyPath().toString())));
    }

    @Test
    void registerRequestShouldFailWhenEmailInvalid() {
        RegisterUserRequest request = new RegisterUserRequest("Alice", "invalid-email", "+14155552671");

        Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "email".equals(v.getPropertyPath().toString())));
    }

    @Test
    void registerRequestShouldFailWhenPhoneInvalid() {
        RegisterUserRequest request = new RegisterUserRequest("Alice", "alice@example.com", "abc");

        Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "phone".equals(v.getPropertyPath().toString())));
    }

    @Test
    void updateRequestShouldPassWhenFieldsOmitted() {
        UpdateUserRequest request = new UpdateUserRequest(null, null);

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
