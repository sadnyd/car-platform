package com.carplatform.order.service;

import com.carplatform.order.dto.OrderResponse;
import com.carplatform.order.dto.CreateOrderRequest;
import com.carplatform.order.dto.UpdateOrderStatusRequest;
import com.carplatform.order.model.Order;
import com.carplatform.order.model.OrderStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-Memory Implementation of Order Service
 * 
 * Uses a HashMap to store orders for Phase 3 (no database).
 * Manages order lifecycle: CREATED -> CONFIRMED -> PROCESSING ->
 * COMPLETED/CANCELLED
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final Map<UUID, Order> orderRepository = new HashMap<>();

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Calculate reservation expiry based on minutes provided
        Instant reservationExpiry = Instant.now().plusSeconds(request.reservationExpiryMinutes() * 60L);

        // Use placeholder price (will be retrieved from catalog in Phase 4)
        Order order = Order.createNew(
                request.carId(),
                request.userId(),
                java.math.BigDecimal.ZERO,
                reservationExpiry);

        orderRepository.put(order.orderId(), order);
        return mapToResponse(order);
    }

    @Override
    public Optional<OrderResponse> getOrderById(UUID orderId) {
        return Optional.ofNullable(orderRepository.get(orderId))
                .map(this::mapToResponse);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        return orderRepository.values()
                .stream()
                .filter(order -> order.userId().equals(userId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByCarId(UUID carId) {
        return orderRepository.values()
                .stream()
                .filter(order -> order.carId().equals(carId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> listAllOrders() {
        return orderRepository.values()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.get(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        // Check if order is in a valid state for status change
        if (order.status() == OrderStatus.COMPLETED || order.status() == OrderStatus.CANCELLED ||
                order.status() == OrderStatus.FAILED) {
            throw new RuntimeException("Cannot update status of order in terminal state: " + order.status());
        }

        // Check if reservation expired before transitioning to PROCESSING
        if (request.status() == OrderStatus.PROCESSING && order.isReservationExpired()) {
            throw new RuntimeException("Reservation has expired. Cannot proceed to PROCESSING");
        }

        Order updatedOrder = order.withStatus(request.status());
        orderRepository.put(orderId, updatedOrder);
        return mapToResponse(updatedOrder);
    }

    @Override
    public OrderResponse cancelOrder(UUID orderId) {
        Order order = orderRepository.get(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        if (order.status() != OrderStatus.CREATED && order.status() != OrderStatus.CONFIRMED) {
            throw new RuntimeException(
                    "Can only cancel orders in CREATED or CONFIRMED state. Current: " + order.status());
        }

        Order cancelledOrder = order.withStatus(OrderStatus.CANCELLED);
        orderRepository.put(orderId, cancelledOrder);
        return mapToResponse(cancelledOrder);
    }

    @Override
    public boolean isReservationExpired(UUID orderId) {
        Order order = orderRepository.get(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        return order.isReservationExpired();
    }

    /**
     * Convert Order model to OrderResponse DTO
     */
    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
                order.orderId(),
                order.carId(),
                order.userId(),
                order.priceAtPurchase(),
                order.status(),
                order.orderDate(),
                order.reservationExpiry(),
                order.lastUpdated());
    }
}
