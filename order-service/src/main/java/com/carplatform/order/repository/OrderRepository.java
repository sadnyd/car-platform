package com.carplatform.order.repository;

import com.carplatform.order.model.Order;
import com.carplatform.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Order entity persistence operations.
 * Extends JpaRepository to provide CRUD and query capabilities.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Find all orders by user ID.
     * 
     * @param userId the user identifier
     * @return list of orders for the user
     */
    List<Order> findByUserId(UUID userId);

    /**
     * Find all orders by car ID.
     * 
     * @param carId the car identifier
     * @return list of orders for the car
     */
    List<Order> findByCarId(UUID carId);

    /**
     * Find all orders by status.
     * 
     * @param status the order status
     * @return list of orders with the given status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find orders by user ID and status.
     * 
     * @param userId the user identifier
     * @param status the order status
     * @return list of orders for the user with the given status
     */
    List<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);
}
