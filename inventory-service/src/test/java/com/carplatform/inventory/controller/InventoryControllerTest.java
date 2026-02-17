package com.carplatform.inventory.controller;

import com.carplatform.inventory.dto.AvailabilityCheckResponse;
import com.carplatform.inventory.dto.CreateInventoryRequest;
import com.carplatform.inventory.dto.InventoryResponse;
import com.carplatform.inventory.dto.ReservationResponse;
import com.carplatform.inventory.exception.GlobalExceptionHandler;
import com.carplatform.inventory.exception.ResourceNotFoundException;
import com.carplatform.inventory.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InventoryController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("InventoryController API Tests")
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void createInventoryShouldReturn201() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID carId = UUID.randomUUID();

        InventoryResponse response = new InventoryResponse(inventoryId, carId, 10, 0, "warehouse-a", Instant.now());
        when(inventoryService.createInventory(any(CreateInventoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carId\":\"" + carId + "\",\"availableUnits\":10,\"location\":\"warehouse-a\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inventoryId").value(inventoryId.toString()));
    }

    @Test
    void createInventoryShouldReturn400OnInvalidPayload() throws Exception {
        UUID carId = UUID.randomUUID();

        mockMvc.perform(post("/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carId\":\"" + carId + "\",\"availableUnits\":-1,\"location\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInventoryShouldReturn404WhenMissing() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/inventory/{inventoryId}", inventoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkAvailabilityShouldReturn200WhenAvailable() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        InventoryResponse response = new InventoryResponse(inventoryId, carId, 5, 2, "warehouse-a", Instant.now());
        when(inventoryService.getInventoryByCarId(carId)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/inventory/check-availability/{carId}", carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void checkAvailabilityShouldReturn409WhenOutOfStock() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        InventoryResponse response = new InventoryResponse(inventoryId, carId, 0, 3, "warehouse-a", Instant.now());
        when(inventoryService.getInventoryByCarId(carId)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/inventory/check-availability/{carId}", carId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_STOCK"));
    }

    @Test
    void reserveInventoryShouldReturn409WhenInsufficientStock() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        InventoryResponse response = new InventoryResponse(inventoryId, carId, 1, 0, "warehouse-a", Instant.now());
        when(inventoryService.getInventoryByCarId(carId)).thenReturn(Optional.of(response));

        mockMvc.perform(post("/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carId\":\"" + carId + "\",\"orderId\":\"order-1\",\"units\":2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("RESERVATION_FAILED"));
    }
}
