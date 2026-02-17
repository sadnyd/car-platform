package com.carplatform.order.client;

import com.carplatform.order.dto.CarDetailsResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CatalogServiceClient Contract Tests")
class CatalogServiceClientContractTest {

    private MockWebServer mockWebServer;
    private CatalogServiceClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        client = new CatalogServiceClient(WebClient.builder().build(), mockWebServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getCarDetailsShouldParseExpectedPayload() {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(
                        "{\"carId\":\"c1\",\"brand\":\"Toyota\",\"model\":\"Camry\",\"year\":2025,\"price\":25000.0,\"status\":\"AVAILABLE\",\"description\":\"Sedan\"}"));

        CarDetailsResponse response = client.getCarDetails("c1").block();

        assertNotNull(response);
        assertEquals("Toyota", response.getBrand());
        assertEquals(25000.0, response.getPrice());
    }
}
