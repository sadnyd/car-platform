package com.carplatform.order.controller;

import com.carplatform.order.dto.CreateOrderRequest;
import com.carplatform.order.dto.OrderResponse;
import com.carplatform.order.model.OrderStatus;
import com.carplatform.order.service.OrderOrchestrationService;
import com.carplatform.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrdersController.class)
@DisplayName("OrdersController API Tests")
class OrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderOrchestrationService orchestrationService;

    @Test
    void createOrderShouldReturn201WhenSuccessful() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        OrderResponse response = new OrderResponse(orderId, carId, userId, BigDecimal.TEN,
                OrderStatus.INVENTORY_RESERVED, Instant.now(), Instant.now().plusSeconds(60), Instant.now());

        when(orchestrationService.createOrderWithInventoryValidation(any(CreateOrderRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carId\":\"" + carId + "\",\"userId\":\"" + userId + "\",\"reservationExpiryMinutes\":30}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("INVENTORY_RESERVED"));
    }

    @Test
    void createOrderShouldReturn400OnValidationError() throws Exception {
        UUID carId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carId\":\"" + carId + "\",\"userId\":\"" + userId + "\",\"reservationExpiryMinutes\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrderShouldReturn409WhenInventoryUnavailable() throws Exception {
        UUID carId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(orchestrationService.createOrderWithInventoryValidation(any(CreateOrderRequest.class)))
                .thenThrow(
                        new OrderOrchestrationService.OrderCreationException("Out of stock", "INVENTORY_UNAVAILABLE"));

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carId\":\"" + carId + "\",\"userId\":\"" + userId + "\",\"reservationExpiryMinutes\":30}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.orderId").doesNotExist());
    }

    @Test
    void getOrderShouldReturn404WhenMissing() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderService.getOrderById(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isNotFound());
    }
}
