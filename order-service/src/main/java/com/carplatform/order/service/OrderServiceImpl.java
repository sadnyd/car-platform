package com.carplatform.order.service;

import com.carplatform.order.dto.OrderResponse;
import com.carplatform.order.dto.CreateOrderRequest;
import com.carplatform.order.dto.UpdateOrderStatusRequest;
import com.carplatform.order.model.Order;
import com.carplatform.order.model.OrderStatus;
import com.carplatform.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Database-backed Implementation of Order Service
 * 
 * Uses OrderRepository (Spring Data JPA) to persist orders in PostgreSQL.
 * Manages order lifecycle: CREATED -> CONFIRMED -> PROCESSING ->
 * COMPLETED/CANCELLED
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Calculate reservation expiry based on minutes provided
        Instant reservationExpiry = Instant.now().plusSeconds(request.reservationExpiryMinutes() * 60L);

        // Create new order using factory method
        Order order = Order.createNew(
                request.carId(),
                request.userId(),
                java.math.BigDecimal.ZERO,
                reservationExpiry);

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    @Override
    public Optional<OrderResponse> getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(this::mapToResponse);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByCarId(UUID carId) {
        return orderRepository.findByCarId(carId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> listAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Check if order is in a valid state for status change
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED ||
                order.getStatus() == OrderStatus.FAILED) {
            throw new RuntimeException("Cannot update status of order in terminal state: " + order.getStatus());
        }

        // Check if reservation expired before transitioning to PROCESSING
        if (request.status() == OrderStatus.PROCESSING && order.isReservationExpired()) {
            throw new RuntimeException("Reservation has expired. Cannot proceed to PROCESSING");
        }

        order.setStatus(request.status());
        order.setLastUpdated(Instant.now());
        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }

    @Override
    public OrderResponse cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException(
                    "Can only cancel orders in CREATED or CONFIRMED state. Current: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setLastUpdated(Instant.now());
        Order cancelledOrder = orderRepository.save(order);
        return mapToResponse(cancelledOrder);
    }

    @Override
    public boolean isReservationExpired(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return order.isReservationExpired();
    }

    /**
     * Convert Order model to OrderResponse DTO
     */
    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getCarId(),
                order.getUserId(),
                order.getPriceAtPurchase(),
                order.getStatus(),
                order.getOrderDate(),
                order.getReservationExpiry(),
                order.getLastUpdated());
    }
}
