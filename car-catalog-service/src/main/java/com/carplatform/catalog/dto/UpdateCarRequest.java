package com.carplatform.catalog.dto;

import com.carplatform.catalog.model.CarStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateCarRequest(
        String brand,
        String model,
        String variant,
        @Min(1990) Integer manufacturingYear,
        @Min(1) BigDecimal price,
        String description,
        @NotNull CarStatus status) {
}
