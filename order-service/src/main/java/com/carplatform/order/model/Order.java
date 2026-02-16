package com.carplatform.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Order domain model representing a car purchase order.
 * JPA-managed entity for persistence.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "car_id", nullable = false)
    private UUID carId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "price_at_purchase", nullable = false, precision = 19, scale = 2)
    private BigDecimal priceAtPurchase;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    @Column(name = "reservation_expiry")
    private Instant reservationExpiry;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    // Constructors
    public Order() {
    }

    public Order(UUID orderId, UUID carId, UUID userId, BigDecimal priceAtPurchase,
            OrderStatus status, Instant orderDate, Instant reservationExpiry, Instant lastUpdated) {
        this.orderId = orderId;
        this.carId = carId;
        this.userId = userId;
        this.priceAtPurchase = priceAtPurchase;
        this.status = status;
        this.orderDate = orderDate;
        this.reservationExpiry = reservationExpiry;
        this.lastUpdated = lastUpdated;
    }

    /**
     * Factory method to create a new order with initial status CREATED.
     */
    public static Order createNew(UUID carId, UUID userId, BigDecimal price, Instant reservationExpiry) {
        Order order = new Order();
        // Don't set orderId - let Hibernate/JPA generate it with @GeneratedValue
        order.setCarId(carId);
        order.setUserId(userId);
        order.setPriceAtPurchase(price);
        order.setStatus(OrderStatus.CREATED);
        order.setOrderDate(Instant.now());
        order.setReservationExpiry(reservationExpiry);
        order.setLastUpdated(Instant.now());
        return order;
    }

    /**
     * Create a new order with updated status.
     */
    public Order withStatus(OrderStatus newStatus) {
        Order updated = new Order(
                this.orderId,
                this.carId,
                this.userId,
                this.priceAtPurchase,
                newStatus,
                this.orderDate,
                this.reservationExpiry,
                Instant.now());
        return updated;
    }

    /**
     * Check if reservation has expired.
     */
    public boolean isReservationExpired() {
        return Instant.now().isAfter(reservationExpiry);
    }

    // Getters and Setters
    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getCarId() {
        return carId;
    }

    public void setCarId(UUID carId) {
        this.carId = carId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Instant orderDate) {
        this.orderDate = orderDate;
    }

    public Instant getReservationExpiry() {
        return reservationExpiry;
    }

    public void setReservationExpiry(Instant reservationExpiry) {
        this.reservationExpiry = reservationExpiry;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
