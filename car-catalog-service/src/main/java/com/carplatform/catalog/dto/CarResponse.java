package com.carplatform.catalog.dto;

import com.carplatform.catalog.model.CarStatus;
import com.carplatform.catalog.model.FuelType;
import com.carplatform.catalog.model.TransmissionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CarResponse(
        UUID carId,
        String brand,
        String model,
        String variant,
        int manufacturingYear,
        FuelType fuelType,
        TransmissionType transmissionType,
        BigDecimal price,
        CarStatus status,
        String description,
        Instant createdAt) {
}
