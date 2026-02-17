package com.carplatform.order.repository;

import com.carplatform.order.model.Order;
import com.carplatform.order.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderRepository JPA Tests")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldSaveAndFindById() {
        Order saved = orderRepository.save(buildOrder(UUID.randomUUID(), UUID.randomUUID(), OrderStatus.CREATED));

        assertTrue(orderRepository.findById(saved.getOrderId()).isPresent());
    }

    @Test
    void shouldFindByUserId() {
        UUID userId = UUID.randomUUID();
        orderRepository.save(buildOrder(UUID.randomUUID(), userId, OrderStatus.CREATED));
        orderRepository.save(buildOrder(UUID.randomUUID(), userId, OrderStatus.CONFIRMED));

        List<Order> results = orderRepository.findByUserId(userId);

        assertEquals(2, results.size());
    }

    @Test
    void shouldFindByStatus() {
        orderRepository.save(buildOrder(UUID.randomUUID(), UUID.randomUUID(), OrderStatus.CREATED));
        orderRepository.save(buildOrder(UUID.randomUUID(), UUID.randomUUID(), OrderStatus.COMPLETED));

        List<Order> completed = orderRepository.findByStatus(OrderStatus.COMPLETED);

        assertEquals(1, completed.size());
        assertEquals(OrderStatus.COMPLETED, completed.get(0).getStatus());
    }

    @Test
    void shouldReturnEmptyWhenNoMatchingRows() {
        List<Order> result = orderRepository.findByUserIdAndStatus(UUID.randomUUID(), OrderStatus.CANCELLED);

        assertTrue(result.isEmpty());
    }

    private Order buildOrder(UUID carId, UUID userId, OrderStatus status) {
        Order order = new Order();
        order.setCarId(carId);
        order.setUserId(userId);
        order.setStatus(status);
        order.setPriceAtPurchase(BigDecimal.valueOf(12345));
        order.setOrderDate(Instant.now());
        order.setReservationExpiry(Instant.now().plusSeconds(3600));
        order.setLastUpdated(Instant.now());
        return order;
    }
}
