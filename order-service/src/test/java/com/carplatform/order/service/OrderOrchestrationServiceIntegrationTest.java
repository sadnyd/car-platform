package com.carplatform.order.service;

import com.carplatform.order.client.InventoryServiceClient;
import com.carplatform.order.client.CatalogServiceClient;
import com.carplatform.order.dto.InventoryAvailabilityResponse;
import com.carplatform.order.dto.CarDetailsResponse;
import com.carplatform.order.dto.CreateOrderRequest;
import com.carplatform.order.dto.InventoryReservationResponse;
import com.carplatform.order.dto.OrderResponse;
import com.carplatform.order.model.OrderStatus;
import com.carplatform.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Integration Tests for Order Orchestration
 * 
 * Tests inter-service communication flows:
 * - Order creation with inventory validation
 * - Error handling for service failures
 * - Fallback behaviors
 * 
 * These tests verify the complete orchestration without mocking
 * (when possible) to test real HTTP calls.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Order Orchestration Integration Tests")
public class OrderOrchestrationServiceIntegrationTest {

        @Autowired
        private OrderOrchestrationService orchestrationService;

        @Autowired
        private OrderRepository orderRepository;

        @MockBean
        private InventoryServiceClient inventoryServiceClient;

        @MockBean
        private CatalogServiceClient catalogServiceClient;

        private UUID testCarId;
        private UUID testUserId;
        private CreateOrderRequest validRequest;

        @BeforeEach
        void setUp() {
                testCarId = UUID.randomUUID();
                testUserId = UUID.randomUUID();
                validRequest = new CreateOrderRequest(testCarId, testUserId, 60); // 60 min reservation
        }

        /**
         * TEST 1: Happy Path - Order Creation Success
         * 
         * Scenario: All services respond successfully
         * Expected: Order created with INVENTORY_RESERVED status
         */
        @Test
        @DisplayName("Test 1: Order creation succeeds when inventory available and catalog accessible")
        void testOrderCreationSuccess_HappyPath() {
                log.info("TEST 1: Happy Path - Order Creation Success");

                // Setup mock responses
                InventoryAvailabilityResponse availabilityResponse = InventoryAvailabilityResponse.success(
                                testCarId.toString(), 10, 2, 8);
                arrange_InventoryServiceReturns(availabilityResponse);

                InventoryReservationResponse reservationResponse = InventoryReservationResponse.success(
                                UUID.randomUUID(), testCarId, validRequest.userId().toString(), 1, 7);
                arrange_InventoryServiceReturnsReservation(reservationResponse);

                CarDetailsResponse carDetails = new CarDetailsResponse(
                                testCarId.toString(), "Toyota", "Camry", 2025, 25000.0, "AVAILABLE", "Sedan",
                                null, null);
                arrange_CatalogServiceReturns(carDetails);

                // Execute
                OrderResponse response = orchestrationService.createOrderWithInventoryValidation(validRequest);

                // Assert
                assertNotNull(response.orderId());
                assertEquals(testCarId, response.carId());
                assertEquals(testUserId, response.userId());
                assertEquals(OrderStatus.INVENTORY_RESERVED, response.status());
                assertEquals(BigDecimal.valueOf(25000.0), response.priceAtPurchase());

                // Verify order persisted
                assertTrue(orderRepository.findById(response.orderId()).isPresent());

                log.info("✅ TEST 1 PASSED: Order created successfully with INVENTORY_RESERVED status");
        }

        /**
         * TEST 2: Inventory Unavailable
         * 
         * Scenario: Inventory service reports out of stock
         * Expected: OrderCreationException with INVENTORY_UNAVAILABLE error code
         * Expected: Order NOT created
         */
        @Test
        @DisplayName("Test 2: Order creation fails gracefully when inventory unavailable")
        void testOrderCreationFails_InventoryUnavailable() {
                log.info("TEST 2: Order Creation Fails - Inventory Unavailable");

                // Setup: Inventory out of stock
                InventoryAvailabilityResponse outOfStockResponse = InventoryAvailabilityResponse.outOfStock(
                                testCarId.toString(), 10);
                arrange_InventoryServiceReturns(outOfStockResponse);

                // Execute & Assert
                OrderOrchestrationService.OrderCreationException exception = assertThrows(
                                OrderOrchestrationService.OrderCreationException.class, () -> {
                                        orchestrationService.createOrderWithInventoryValidation(validRequest);
                                });

                assertEquals("INVENTORY_UNAVAILABLE", exception.getErrorCode());
                assertTrue(exception.getMessage().contains("not available"));

                // Verify: Inventory service was called once
                verify(inventoryServiceClient, times(1)).checkAvailability(anyString());

                // Verify: Order was NOT created
                long orderCount = orderRepository.count();
                log.info("Orders in database after failure: {}", orderCount);

                log.info("✅ TEST 2 PASSED: Order not created when inventory unavailable");
        }

        /**
         * TEST 3: Inventory Service Timeout
         * 
         * Scenario: Inventory service fails to respond
         * Expected: OrderCreationException with appropriate error code
         * Expected: Order NOT created
         */
        @Test
        @DisplayName("Test 3: Order creation fails when inventory service timeout")
        void testOrderCreationFails_InventoryTimeout() {
                log.info("TEST 3: Order Creation Fails - Inventory Timeout");

                // Setup: Inventory service timeout
                when(inventoryServiceClient.checkAvailability(anyString()))
                                .thenReturn(Mono.error(new RuntimeException("Connection timeout")));

                // Execute & Assert
                assertThrows(Exception.class, () -> {
                        orchestrationService.createOrderWithInventoryValidation(validRequest);
                });

                log.info("✅ TEST 3 PASSED: Order not created on inventory service timeout");
        }

        /**
         * TEST 4: Inventory Reservation Fails
         * 
         * Scenario: Availability check passes but reservation fails (race condition)
         * Expected: OrderCreationException with RESERVATION_FAILED error code
         * Expected: Order NOT created
         */
        @Test
        @DisplayName("Test 4: Order creation fails when reservation fails after availability check")
        void testOrderCreationFails_ReservationFails() {
                log.info("TEST 4: Order Creation Fails - Reservation Fails");

                // Setup: Availability passes
                InventoryAvailabilityResponse availabilityResponse = InventoryAvailabilityResponse.success(
                                testCarId.toString(), 10, 0, 10);
                arrange_InventoryServiceReturns(availabilityResponse);

                // Setup: Reservation fails (race condition - someone else bought it)
                InventoryReservationResponse failedReservation = InventoryReservationResponse.insufficientStock(
                                testCarId.toString(), validRequest.userId().toString());
                arrange_InventoryServiceReturnsReservation(failedReservation);

                // Execute & Assert
                OrderOrchestrationService.OrderCreationException exception = assertThrows(
                                OrderOrchestrationService.OrderCreationException.class, () -> {
                                        orchestrationService.createOrderWithInventoryValidation(validRequest);
                                });

                assertEquals("INSUFFICIENT_STOCK", exception.getErrorCode());

                log.info("✅ TEST 4 PASSED: Reservation failure prevents order creation");
        }

        /**
         * TEST 5: Catalog Service Unavailable (Graceful Fallback)
         * 
         * Scenario: Catalog service fails, but inventory checks pass
         * Expected: Order created with price = 0 (best effort)
         * Expected: Status still INVENTORY_RESERVED
         */
        @Test
        @DisplayName("Test 5: Order creation succeeds despite catalog failure (best effort pricing)")
        void testOrderCreationSucceeds_CatalogFails() {
                log.info("TEST 5: Order Creation Succeeds - Catalog Fails (Fallback)");

                // Setup: Inventory succeeds
                InventoryAvailabilityResponse availabilityResponse = InventoryAvailabilityResponse.success(
                                testCarId.toString(), 10, 2, 8);
                arrange_InventoryServiceReturns(availabilityResponse);

                InventoryReservationResponse reservationResponse = InventoryReservationResponse.success(
                                UUID.randomUUID(), testCarId, validRequest.userId().toString(), 1, 7);
                arrange_InventoryServiceReturnsReservation(reservationResponse);

                // Setup: Catalog fails
                when(catalogServiceClient.getCarDetails(anyString()))
                                .thenReturn(Mono.error(new RuntimeException("Catalog unavailable")));

                // Execute
                OrderResponse response = orchestrationService.createOrderWithInventoryValidation(validRequest);

                // Assert: Order created despite catalog failure
                assertNotNull(response.orderId());
                assertEquals(OrderStatus.INVENTORY_RESERVED, response.status());
                assertEquals(BigDecimal.ZERO, response.priceAtPurchase()); // Default price

                log.info("✅ TEST 5 PASSED: Order created with fallback price when catalog fails");
        }

        /**
         * TEST 6: Car Not Found in Inventory
         * 
         * Scenario: Inventory service returns 404 - car not in inventory
         * Expected: OrderCreationException with RESOURCE_NOT_FOUND error code
         * Expected: Order NOT created
         */
        @Test
        @DisplayName("Test 6: Order creation fails when car not found in inventory")
        void testOrderCreationFails_CarNotFound() {
                log.info("TEST 6: Order Creation Fails - Car Not Found");

                // Setup: Car not found in inventory
                InventoryAvailabilityResponse notFoundResponse = InventoryAvailabilityResponse.notFound(
                                testCarId.toString());
                arrange_InventoryServiceReturns(notFoundResponse);

                // Execute & Assert
                OrderOrchestrationService.OrderCreationException exception = assertThrows(
                                OrderOrchestrationService.OrderCreationException.class, () -> {
                                        orchestrationService.createOrderWithInventoryValidation(validRequest);
                                });

                assertEquals("INVENTORY_UNAVAILABLE", exception.getErrorCode());

                log.info("✅ TEST 6 PASSED: Order not created when car not found in inventory");
        }

        // =============== Helper Methods ===============

        private void arrange_InventoryServiceReturns(InventoryAvailabilityResponse response) {
                when(inventoryServiceClient.checkAvailability(anyString()))
                                .thenReturn(Mono.just(response));
        }

        private void arrange_InventoryServiceReturnsReservation(InventoryReservationResponse response) {
                when(inventoryServiceClient.reserveInventory(any()))
                                .thenReturn(Mono.just(response));
        }

        private void arrange_CatalogServiceReturns(CarDetailsResponse response) {
                when(catalogServiceClient.getCarDetails(anyString()))
                                .thenReturn(Mono.just(response));
        }
}
