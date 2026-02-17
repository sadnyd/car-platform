package com.carplatform.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.UUID;

public record CreateOrderRequest(
                @NotNull UUID carId,
                @NotNull UUID userId,
                @NotNull @Min(1) @Max(1440) Integer reservationExpiryMinutes) {
}
