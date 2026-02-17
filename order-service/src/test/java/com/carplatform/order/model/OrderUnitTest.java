package com.carplatform.order.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Domain Unit Tests")
class OrderUnitTest {

    @Test
    void createNewShouldInitializeRequiredFields() {
        UUID carId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant reservationExpiry = Instant.now().plusSeconds(3600);

        Order order = Order.createNew(carId, userId, BigDecimal.TEN, reservationExpiry);

        assertNull(order.getOrderId());
        assertEquals(carId, order.getCarId());
        assertEquals(userId, order.getUserId());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(BigDecimal.TEN, order.getPriceAtPurchase());
        assertNotNull(order.getOrderDate());
        assertNotNull(order.getLastUpdated());
    }

    @Test
    void withStatusShouldCreateNewObjectWithUpdatedStatus() {
        Order original = new Order();
        original.setOrderId(UUID.randomUUID());
        original.setCarId(UUID.randomUUID());
        original.setUserId(UUID.randomUUID());
        original.setPriceAtPurchase(BigDecimal.ONE);
        original.setStatus(OrderStatus.CREATED);
        original.setOrderDate(Instant.now().minusSeconds(60));
        original.setReservationExpiry(Instant.now().plusSeconds(300));
        original.setLastUpdated(Instant.now().minusSeconds(60));

        Order updated = original.withStatus(OrderStatus.CONFIRMED);

        assertNotSame(original, updated);
        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
        assertEquals(original.getOrderId(), updated.getOrderId());
        assertTrue(updated.getLastUpdated().isAfter(original.getLastUpdated()));
    }

    @Test
    void isReservationExpiredShouldReturnTrueWhenPastExpiry() {
        Order order = new Order();
        order.setReservationExpiry(Instant.now().minusSeconds(1));

        assertTrue(order.isReservationExpired());
    }

    @Test
    void isReservationExpiredShouldReturnFalseWhenFutureExpiry() {
        Order order = new Order();
        order.setReservationExpiry(Instant.now().plusSeconds(60));

        assertFalse(order.isReservationExpired());
    }
}
