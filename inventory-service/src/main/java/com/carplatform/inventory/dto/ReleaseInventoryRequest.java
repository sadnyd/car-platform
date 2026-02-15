package com.carplatform.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReleaseInventoryRequest(
        @NotNull UUID carId,
        @Min(1) int units) {
}
