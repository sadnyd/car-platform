package com.carplatform.gateway.controller;

import com.carplatform.gateway.dto.CarDetailsAggregatedResponse;
import com.carplatform.gateway.dto.CarListingAggregatedResponse;
import com.carplatform.gateway.service.AggregationService;
import com.carplatform.gateway.exception.ResourceNotFoundException;
import com.carplatform.gateway.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration Tests for AggregationController
 * 
 * 
 * Tests REST endpoints with:
 * - Valid requests
 * - Error scenarios
 * - Response format validation
 * - HTTP status codes
 */
@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("AggregationController Integration Tests")
class AggregationControllerIntegrationTest {

        @Autowired
        private WebTestClient webTestClient;

        @MockBean
        private AggregationService aggregationService;

        private UUID testCarId;
        private CarDetailsAggregatedResponse testDetailsResponse;
        private CarListingAggregatedResponse testListingResponse;

        @BeforeEach
        void setUp() {
                testCarId = UUID.randomUUID();

                // Create test details response
                testDetailsResponse = new CarDetailsAggregatedResponse(
                                testCarId,
                                "Tesla",
                                "Model S",
                                2024,
                                89999.99,
                                "Black",
                                CarDetailsAggregatedResponse.AvailabilityInfo.inStock(
                                                testCarId.toString(), 10, 8, 2),
                                new CarDetailsAggregatedResponse.AggregationMetadata());

                // Create test listing response
                List<CarListingAggregatedResponse.CarListItem> items = new ArrayList<>();
                items.add(new CarListingAggregatedResponse.CarListItem(
                                testCarId, "Tesla", "Model S", 2024, 89999.99, "IN_STOCK", 8));
                items.add(new CarListingAggregatedResponse.CarListItem(
                                UUID.randomUUID(), "BMW", "X5", 2024, 65000.00, "OUT_OF_STOCK", 0));

                testListingResponse = new CarListingAggregatedResponse(
                                items,
                                50,
                                20,
                                1);
        }

        // ===================== CAR DETAILS ENDPOINT TESTS =====================

        @Test
        @DisplayName("GET /cars/{carId}/details should return 200 with aggregated data")
        void testGetCarDetails_Success() {
                // GIVEN: AggregationService returns valid response
                when(aggregationService.getCarDetailsWithAvailability(testCarId))
                                .thenReturn(testDetailsResponse);

                // WHEN & THEN: Call endpoint and verify response
                webTestClient.get()
                                .uri("/cars/{carId}/details", testCarId)
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isOk()
                                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                                .expectBody(CarDetailsAggregatedResponse.class)
                                .consumeWith(response -> {
                                        assert response.getResponseBody() != null;
                                        assert response.getResponseBody().getMake().equals("Tesla");
                                        assert response.getResponseBody().getAvailability().getStatus()
                                                        .equals("IN_STOCK");
                                        assert response.getResponseBody().getAvailability().getAvailableUnits() == 8;
                                });

                // VERIFY: Service was called with correct ID
                verify(aggregationService, times(1)).getCarDetailsWithAvailability(testCarId);
        }

        @Test
        @DisplayName("GET /cars/{carId}/details should return 404 when car not found")
        void testGetCarDetails_NotFound() {
                // GIVEN: Service throws ResourceNotFoundException
                when(aggregationService.getCarDetailsWithAvailability(testCarId))
                                .thenThrow(new ResourceNotFoundException("Car not found", "Car", testCarId.toString()));

                // WHEN & THEN: Call endpoint and verify 404 response
                webTestClient.get()
                                .uri("/cars/{carId}/details", testCarId)
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("GET /cars/{carId}/details should return 206 when inventory unavailable")
        void testGetCarDetails_PartialContent() {
                // GIVEN: Service returns degraded response (inventory unavailable)
                CarDetailsAggregatedResponse degradedResponse = new CarDetailsAggregatedResponse(
                                testCarId,
                                "Tesla",
                                "Model S",
                                2024,
                                89999.99,
                                "Black",
                                CarDetailsAggregatedResponse.AvailabilityInfo.unknown("Service unavailable"),
                                new CarDetailsAggregatedResponse.AggregationMetadata());
                when(aggregationService.getCarDetailsWithAvailability(testCarId))
                                .thenReturn(degradedResponse);

                // WHEN & THEN: Call endpoint
                webTestClient.get()
                                .uri("/cars/{carId}/details", testCarId)
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isOk() // Still 200 with degraded data
                                .expectBody(CarDetailsAggregatedResponse.class)
                                .consumeWith(response -> {
                                        assert response.getResponseBody().getAvailability().getStatus()
                                                        .equals("UNKNOWN");
                                });
        }

        @Test
        @DisplayName("GET /cars/{carId}/details should return 503 on catalog service failure")
        void testGetCarDetails_ServiceUnavailable() {
                // GIVEN: Service throws ServiceUnavailableException
                when(aggregationService.getCarDetailsWithAvailability(testCarId))
                                .thenThrow(new ServiceUnavailableException("Catalog service down", "catalog"));

                // WHEN & THEN: Call endpoint and verify 503 response
                webTestClient.get()
                                .uri("/cars/{carId}/details", testCarId)
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        }

        // ===================== CAR LISTING ENDPOINT TESTS =====================

        @Test
        @DisplayName("GET /cars/listing should return 200 with paginated data")
        void testGetCarListing_Success() {
                // GIVEN: AggregationService returns valid listing
                when(aggregationService.getCarListingWithAvailability(1, 20))
                                .thenReturn(testListingResponse);

                // WHEN & THEN: Call endpoint and verify response
                webTestClient.get()
                                .uri("/cars/listing?page=1&size=20")
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isOk()
                                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                                .expectBody(CarListingAggregatedResponse.class)
                                .consumeWith(response -> {
                                        assert response.getResponseBody() != null;
                                        assert response.getResponseBody().getCars().size() == 2;
                                        assert response.getResponseBody().getPagination().getTotalCount() == 50;
                                        assert response.getResponseBody().getPagination().getTotalPages() == 3;
                                });

                // VERIFY: Service was called with correct parameters
                verify(aggregationService, times(1)).getCarListingWithAvailability(1, 20);
        }

        @Test
        @DisplayName("GET /cars/listing should use default pagination if not provided")
        void testGetCarListing_DefaultPagination() {
                // GIVEN: Service configured to return default page
                when(aggregationService.getCarListingWithAvailability(1, 50))
                                .thenReturn(testListingResponse);

                // WHEN & THEN: Call endpoint without pagination params
                webTestClient.get()
                                .uri("/cars/listing")
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isOk();

                // VERIFY: Service was called with defaults
                verify(aggregationService, times(1)).getCarListingWithAvailability(1, 50);
        }

        @Test
        @DisplayName("GET /cars/listing should return 400 for invalid page number")
        void testGetCarListing_InvalidPageNumber() {
                // WHEN & THEN: Call endpoint with invalid page number (0 or negative)
                webTestClient.get()
                                .uri("/cars/listing?page=0&size=20")
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isBadRequest(); // Should validate input
        }

        @Test
        @DisplayName("GET /cars/listing should return 400 for invalid page size")
        void testGetCarListing_InvalidPageSize() {
                // WHEN & THEN: Call endpoint with invalid size (0 or larger than max)
                webTestClient.get()
                                .uri("/cars/listing?page=1&size=0")
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("GET /cars/listing should return 503 on catalog service failure")
        void testGetCarListing_CatalogServiceDown() {
                // GIVEN: Service throws ServiceUnavailableException
                when(aggregationService.getCarListingWithAvailability(any(), any()))
                                .thenThrow(new ServiceUnavailableException("Catalog service down", "catalog"));

                // WHEN & THEN: Call endpoint and verify 503 response
                webTestClient.get()
                                .uri("/cars/listing?page=1&size=20")
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        }

        // ===================== HEALTH CHECK ENDPOINT TESTS =====================

        @Test
        @DisplayName("GET /cars/aggregation/health should return 200 when healthy")
        void testHealthCheck_Success() {
                // WHEN & THEN: Call health endpoint
                webTestClient.get()
                                .uri("/cars/aggregation/health")
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.status").isEqualTo("UP")
                                .jsonPath("$.timestamp").exists();
        }

        // ===================== RESPONSE VALIDATION TESTS =====================

        @Test
        @DisplayName("Response should include aggregation metadata")
        void testGetCarDetails_MetadataIncluded() {
                // GIVEN: Service returns response with metadata
                when(aggregationService.getCarDetailsWithAvailability(testCarId))
                                .thenReturn(testDetailsResponse);

                // WHEN & THEN: Verify metadata is present
                webTestClient.get()
                                .uri("/cars/{carId}/details", testCarId)
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(CarDetailsAggregatedResponse.class)
                                .consumeWith(response -> {
                                        assert response.getResponseBody().getMetadata() != null;
                                        assert response.getResponseBody().getMetadata().getAggregatedAt() != null;
                                });
        }

        @Test
        @DisplayName("Listing response should include pagination metadata")
        void testGetCarListing_PaginationMetadata() {
                // GIVEN: Service returns listing with pagination
                when(aggregationService.getCarListingWithAvailability(1, 20))
                                .thenReturn(testListingResponse);

                // WHEN & THEN: Verify pagination data
                webTestClient.get()
                                .uri("/cars/listing?page=1&size=20")
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(CarListingAggregatedResponse.class)
                                .consumeWith(response -> {
                                        assert response.getResponseBody().getPagination() != null;
                                        assert response.getResponseBody().getPagination().getCurrentPage() == 1;
                                        assert response.getResponseBody().getPagination().getTotalPages() > 0;
                                });
        }
}
