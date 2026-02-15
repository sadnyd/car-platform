package com.carplatform.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateInventoryRequest(
        @NotNull UUID carId,
        @Min(0) int availableUnits,
        @NotBlank String location) {
}
