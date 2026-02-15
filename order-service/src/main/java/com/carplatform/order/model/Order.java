package com.carplatform.order.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Order domain model representing a car purchase order.
 * This is an immutable domain record.
 * 
 * No database annotations - this is a pure domain model.
 */
public record Order(
        UUID orderId,
        UUID carId,
        UUID userId,
        BigDecimal priceAtPurchase,
        OrderStatus status,
        Instant orderDate,
        Instant reservationExpiry,
        Instant lastUpdated) {

    /**
     * Factory method to create a new order with initial status CREATED.
     */
    public static Order createNew(UUID carId, UUID userId, BigDecimal price, Instant reservationExpiry) {
        return new Order(
                UUID.randomUUID(),
                carId,
                userId,
                price,
                OrderStatus.CREATED,
                Instant.now(),
                reservationExpiry,
                Instant.now());
    }

    /**
     * Transition to a new status.
     */
    public Order withStatus(OrderStatus newStatus) {
        return new Order(
                this.orderId,
                this.carId,
                this.userId,
                this.priceAtPurchase,
                newStatus,
                this.orderDate,
                this.reservationExpiry,
                Instant.now());
    }

    /**
     * Check if reservation has expired.
     */
    public boolean isReservationExpired() {
        return Instant.now().isAfter(reservationExpiry);
    }
}
