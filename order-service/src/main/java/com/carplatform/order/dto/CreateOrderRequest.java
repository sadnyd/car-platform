package com.carplatform.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID carId,
        @NotNull UUID userId,
        @NotNull Integer reservationExpiryMinutes) {
}
