package com.carplatform.order.dto;

import com.carplatform.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID carId,
        UUID userId,
        BigDecimal priceAtPurchase,
        OrderStatus status,
        Instant orderDate,
        Instant reservationExpiry,
        Instant lastUpdated) {
}
