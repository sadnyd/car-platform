package com.carplatform.inventory.integration;

import com.carplatform.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Inventory Full Flow Integration Test")
class InventoryFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void shouldCreateAndReserveThroughControllerServiceRepository() throws Exception {
        UUID carId = UUID.randomUUID();

        mockMvc.perform(post("/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carId\":\"" + carId + "\",\"availableUnits\":5,\"location\":\"warehouse-a\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/inventory/check-availability/{carId}", carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        mockMvc.perform(post("/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carId\":\"" + carId + "\",\"orderId\":\"order-1\",\"units\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitsReserved").value(2));

        assertEquals(1, inventoryRepository.count());
        assertEquals(3, inventoryRepository.findAll().get(0).getAvailableUnits());
        assertEquals(2, inventoryRepository.findAll().get(0).getReservedUnits());
    }
}
