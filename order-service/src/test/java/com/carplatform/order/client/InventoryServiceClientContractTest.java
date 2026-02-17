package com.carplatform.order.client;

import com.carplatform.order.dto.InventoryAvailabilityResponse;
import com.carplatform.order.dto.InventoryReservationRequest;
import com.carplatform.order.dto.InventoryReservationResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InventoryServiceClient Contract Tests")
class InventoryServiceClientContractTest {

    private MockWebServer mockWebServer;
    private InventoryServiceClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        client = new InventoryServiceClient(WebClient.builder().build(), mockWebServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void checkAvailabilityShouldParseExpectedFields() {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(
                        "{\"carId\":\"abc\",\"available\":true,\"totalUnits\":10,\"reservedUnits\":2,\"availableUnits\":8}"));

        InventoryAvailabilityResponse response = client.checkAvailability("abc").block();

        assertNotNull(response);
        assertTrue(response.isAvailable());
        assertEquals(8, response.getAvailableUnits());
    }

    @Test
    void reserveInventoryShouldParseReservationResponse() {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(
                        "{\"reservationId\":\"r1\",\"carId\":\"c1\",\"orderId\":\"o1\",\"unitsReserved\":1,\"unitsRemaining\":9,\"status\":\"RESERVED\"}"));

        InventoryReservationRequest request = new InventoryReservationRequest("c1", "o1", 1);
        InventoryReservationResponse response = client.reserveInventory(request).block();

        assertNotNull(response);
        assertEquals("r1", response.getReservationId());
        assertEquals("RESERVED", response.getStatus());
    }
}
