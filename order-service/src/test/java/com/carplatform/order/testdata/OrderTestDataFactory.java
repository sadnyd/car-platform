package com.carplatform.order.testdata;

import com.carplatform.order.dto.CarDetailsResponse;
import com.carplatform.order.dto.CreateOrderRequest;
import com.carplatform.order.dto.InventoryAvailabilityResponse;
import com.carplatform.order.dto.InventoryReservationResponse;
import com.carplatform.order.dto.UpdateOrderStatusRequest;
import com.carplatform.order.model.Order;
import com.carplatform.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class OrderTestDataFactory {

    public static final UUID CAR_ID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    public static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    public static final UUID ORDER_ID = UUID.fromString("3d594650-3436-4f44-8f58-310f5cc72f15");

    private OrderTestDataFactory() {
    }

    public static CreateOrderRequest validCreateOrderRequest() {
        return new CreateOrderRequest(CAR_ID, USER_ID, 60);
    }

    public static UpdateOrderStatusRequest processingStatusRequest() {
        return new UpdateOrderStatusRequest(OrderStatus.PROCESSING);
    }

    public static Order createdOrder() {
        Order order = new Order();
        order.setOrderId(ORDER_ID);
        order.setCarId(CAR_ID);
        order.setUserId(USER_ID);
        order.setPriceAtPurchase(BigDecimal.valueOf(25000));
        order.setStatus(OrderStatus.CREATED);
        order.setOrderDate(Instant.now().minusSeconds(120));
        order.setReservationExpiry(Instant.now().plusSeconds(3600));
        order.setLastUpdated(Instant.now().minusSeconds(30));
        return order;
    }

    public static Order completedOrder() {
        Order order = createdOrder();
        order.setStatus(OrderStatus.COMPLETED);
        return order;
    }

    public static InventoryAvailabilityResponse availableInventory() {
        return InventoryAvailabilityResponse.success(CAR_ID.toString(), 10, 2, 8);
    }

    public static InventoryAvailabilityResponse unavailableInventory() {
        return InventoryAvailabilityResponse.outOfStock(CAR_ID.toString(), 10);
    }

    public static InventoryReservationResponse reservedInventory() {
        return InventoryReservationResponse.success(UUID.randomUUID(), CAR_ID, USER_ID.toString(), 1, 7);
    }

    public static CarDetailsResponse carDetails() {
        return new CarDetailsResponse(CAR_ID.toString(), "Toyota", "Camry", 2025, 25000.0, "AVAILABLE", "Sedan",
                null, null);
    }
}
