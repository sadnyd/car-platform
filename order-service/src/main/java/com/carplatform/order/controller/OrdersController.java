package com.carplatform.order.controller;

import com.carplatform.order.dto.CreateOrderRequest;
import com.carplatform.order.dto.UpdateOrderStatusRequest;
import com.carplatform.order.dto.OrderResponse;
import com.carplatform.order.service.OrderService;
import com.carplatform.order.service.OrderOrchestrationService;
import com.carplatform.order.service.OrderOrchestrationService.OrderCreationException;
import com.carplatform.order.exception.ResourceNotFoundException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Orders REST Controller
 * 
 * PHASE 5 Changes:
 * - POST /orders now uses OrderOrchestrationService
 * - Validates inventory before creating order
 * - Returns different error codes based on failure type
 */
@Slf4j
@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderOrchestrationService orchestrationService;

    /**
     * Create order with inventory validation (PHASE 5)
     * 
     * Workflow:
     * 1. Check inventory availability
     * 2. Reserve inventory
     * 3. Fetch car details
     * 4. Create order (if all previous steps succeed)
     * 
     * Response codes:
     * - 201: Order created successfully (INVENTORY_RESERVED status)
     * - 404: Car not found in inventory
     * - 409: Out of stock
     * - 503: Inventory service unavailable
     * - 500: Unexpected error
     */
    @PostMapping
    @Bulkhead(name = "orderWriteBulkhead", type = Bulkhead.Type.SEMAPHORE)
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        try {
            log.info("Order creation requested - car: {}, user: {}", request.carId(), request.userId());

            OrderResponse response = orchestrationService.createOrderWithInventoryValidation(request);

            log.info("Order created successfully - order ID: {}", response.orderId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (OrderCreationException e) {
            log.warn("Order creation failed - error: {}, message: {}", e.getErrorCode(), e.getMessage());

            // Map error codes to HTTP status codes
            HttpStatus status = mapErrorCodeToStatus(e.getErrorCode());

            // Return error response with appropriate status
            return ResponseEntity.status(status)
                    .body(new OrderResponse(
                            null,
                            request.carId(),
                            request.userId(),
                            null,
                            null,
                            null,
                            null,
                            null));
        } catch (Exception e) {
            log.error("Unexpected error during order creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OrderResponse(
                            null,
                            request.carId(),
                            request.userId(),
                            null,
                            null,
                            null,
                            null,
                            null));
        }
    }

    /**
     * Map error codes to HTTP status codes
     */
    private HttpStatus mapErrorCodeToStatus(String errorCode) {
        return switch (errorCode) {
            case "RESOURCE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "INSUFFICIENT_STOCK" -> HttpStatus.CONFLICT;
            case "INVENTORY_UNAVAILABLE", "RESERVATION_FAILED" -> HttpStatus.CONFLICT;
            case "SERVICE_UNAVAILABLE" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    @GetMapping("/{orderId}")
    @Bulkhead(name = "orderReadBulkhead", type = Bulkhead.Type.SEMAPHORE)
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @GetMapping
    @Bulkhead(name = "orderReadBulkhead", type = Bulkhead.Type.SEMAPHORE)
    public ResponseEntity<List<OrderResponse>> listAll() {
        return ResponseEntity.ok(orderService.listAllOrders());
    }

    @GetMapping("/user/{userId}")
    @Bulkhead(name = "orderReadBulkhead", type = Bulkhead.Type.SEMAPHORE)
    public ResponseEntity<List<OrderResponse>> getUserOrders(@PathVariable UUID userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PutMapping("/{orderId}/status")
    @Bulkhead(name = "orderWriteBulkhead", type = Bulkhead.Type.SEMAPHORE)
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID orderId,
            @RequestBody @Valid UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request));
    }

    @PostMapping("/{orderId}/cancel")
    @Bulkhead(name = "orderWriteBulkhead", type = Bulkhead.Type.SEMAPHORE)
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}
