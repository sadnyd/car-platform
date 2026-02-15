package com.carplatform.order.service;

import com.carplatform.order.dto.OrderResponse;
import com.carplatform.order.dto.CreateOrderRequest;
import com.carplatform.order.dto.UpdateOrderStatusRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Order Service Interface
 * 
 * Defines the contract for order management operations.
 * Handles order lifecycle from creation through completion/cancellation.
 */
public interface OrderService {

    /**
     * Create a new order
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Get order by ID
     */
    Optional<OrderResponse> getOrderById(UUID orderId);

    /**
     * Get orders by user ID
     */
    List<OrderResponse> getOrdersByUserId(UUID userId);

    /**
     * Get orders by car ID
     */
    List<OrderResponse> getOrdersByCarId(UUID carId);

    /**
     * List all orders
     */
    List<OrderResponse> listAllOrders();

    /**
     * Update order status
     */
    OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request);

    /**
     * Cancel order (if in CREATED or CONFIRMED state)
     */
    OrderResponse cancelOrder(UUID orderId);

    /**
     * Check if reservation has expired
     */
    boolean isReservationExpired(UUID orderId);
}
