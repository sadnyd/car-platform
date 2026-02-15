package com.carplatform.user.dto;

import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
        String name,
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format") String phone) {
}
