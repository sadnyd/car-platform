package com.carplatform.catalog.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Car(
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
