package com.carplatform.order.integration;

import com.carplatform.order.client.CatalogServiceClient;
import com.carplatform.order.client.InventoryServiceClient;
import com.carplatform.order.dto.CarDetailsResponse;
import com.carplatform.order.dto.InventoryAvailabilityResponse;
import com.carplatform.order.dto.InventoryReservationResponse;
import com.carplatform.order.model.Order;
import com.carplatform.order.model.OrderStatus;
import com.carplatform.order.repository.OrderRepository;
import com.carplatform.order.testdata.OrderTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Order Full Flow Integration Test")
class OrderCreateFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private InventoryServiceClient inventoryServiceClient;

    @MockBean
    private CatalogServiceClient catalogServiceClient;

    @Test
    void shouldCreateOrderThroughControllerServiceRepositoryFlow() throws Exception {
        InventoryAvailabilityResponse availability = OrderTestDataFactory.availableInventory();
        InventoryReservationResponse reservation = OrderTestDataFactory.reservedInventory();
        CarDetailsResponse details = OrderTestDataFactory.carDetails();

        when(inventoryServiceClient.checkAvailability(anyString())).thenReturn(Mono.just(availability));
        when(inventoryServiceClient.reserveInventory(any())).thenReturn(Mono.just(reservation));
        when(catalogServiceClient.getCarDetails(anyString())).thenReturn(Mono.just(details));

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carId\":\"" + OrderTestDataFactory.CAR_ID + "\",\"userId\":\""
                        + OrderTestDataFactory.USER_ID + "\",\"reservationExpiryMinutes\":60}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("INVENTORY_RESERVED"));

        assertEquals(1, orderRepository.count());
        Order saved = orderRepository.findAll().get(0);
        assertEquals(OrderStatus.INVENTORY_RESERVED, saved.getStatus());
        assertEquals(OrderTestDataFactory.CAR_ID, saved.getCarId());
    }
}
