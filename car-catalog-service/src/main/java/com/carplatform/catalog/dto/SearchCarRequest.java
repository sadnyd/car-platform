package com.carplatform.catalog.dto;

import com.carplatform.catalog.model.FuelType;
import com.carplatform.catalog.model.TransmissionType;

import java.math.BigDecimal;

public record SearchCarRequest(
        String brand,
        String model,
        FuelType fuelType,
        TransmissionType transmissionType,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minYear) {
}
