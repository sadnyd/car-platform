package com.carplatform.catalog.dto;

import com.carplatform.catalog.model.FuelType;
import com.carplatform.catalog.model.TransmissionType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateCarRequest(
        @NotBlank String brand,
        @NotBlank String model,
        @NotBlank String variant,
        @Min(1990) int manufacturingYear,
        @NotNull FuelType fuelType,
        @NotNull TransmissionType transmissionType,
        @NotNull @Min(1) BigDecimal price,
        String description) {
}
