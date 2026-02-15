package com.carplatform.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateInventoryRequest(
        @NotNull UUID carId,
        @Min(0) int units,
        @NotBlank String location) {
}
