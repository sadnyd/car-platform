package com.carplatform.order.client;

import com.carplatform.order.dto.InventoryAvailabilityResponse;
import com.carplatform.order.dto.InventoryReservationRequest;
import com.carplatform.order.dto.InventoryReservationResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Inventory Service Client
 * 
 * Encapsulates all HTTP calls to Inventory Service
 * - Check availability before order creation
 * - Reserve stock after inventory confirmation
 * - Handle timeouts and failures gracefully
 */
@Slf4j
@Component
public class InventoryServiceClient {

        private final WebClient webClient;
        private final String inventoryBaseUrl;

        public InventoryServiceClient(WebClient webClient,
                        @Value("${services.inventory.base-url:http://localhost:8082}") String inventoryBaseUrl) {
                this.webClient = webClient;
                this.inventoryBaseUrl = inventoryBaseUrl;
        }

        /**
         * Check if inventory is available for a given car
         * 
         * @param carId Car ID to check
         * @return Mono<InventoryAvailabilityResponse> with availability details
         * 
         *         Error handling:
         *         - Timeout: Fails with TimeoutException
         *         - Service down: Fails with connection error
         *         - Car not found: Returns 404
         *         - Out of stock: Returns availability=false
         */
        public Mono<InventoryAvailabilityResponse> checkAvailability(String carId) {
                log.debug("Checking inventory availability for car: {}", carId);

                return webClient.get()
                                .uri(inventoryBaseUrl + "/inventory/check-availability/{carId}", carId)
                                .retrieve()
                                .bodyToMono(InventoryAvailabilityResponse.class)
                                .timeout(Duration.ofSeconds(2))
                                .doOnSuccess(response -> log.info("Availability check success for car {}: available={}",
                                                carId,
                                                response.isAvailable()))
                                .doOnError(error -> log.error("Availability check failed for car {}: {}", carId,
                                                error.getMessage()));
        }

        @CircuitBreaker(name = "inventoryReadCircuitBreaker", fallbackMethod = "checkAvailabilityFallback")
        @Retry(name = "inventoryReadRetry", fallbackMethod = "checkAvailabilityFallback")
        @Bulkhead(name = "inventoryReadBulkhead", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "checkAvailabilityFallback")
        public Mono<InventoryAvailabilityResponse> guardedCheckAvailability(String carId) {
                return checkAvailability(carId);
        }

        /**
         * Reserve inventory for an order
         * 
         * @param request Reservation request (carId, orderId, units)
         * @return Mono<InventoryReservationResponse> with reservation details
         * 
         *         Error handling:
         *         - Insufficient stock: 409 Conflict
         *         - Car not found: 404 Not Found
         *         - Service error: 5xx
         */
        public Mono<InventoryReservationResponse> reserveInventory(InventoryReservationRequest request) {
                log.debug("Reserving inventory - car: {}, order: {}, units: {}",
                                request.getCarId(), request.getOrderId(), request.getUnits());

                return webClient.post()
                                .uri(inventoryBaseUrl + "/inventory/reserve")
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(InventoryReservationResponse.class)
                                .timeout(Duration.ofSeconds(2))
                                .doOnSuccess(response -> log.info("Reservation success - reservation: {}, units: {}",
                                                response.getReservationId(), response.getUnitsReserved()))
                                .doOnError(error -> log.error("Reservation failed for order {}: {}",
                                                request.getOrderId(), error.getMessage()));
        }

        @CircuitBreaker(name = "inventoryReserveCircuitBreaker", fallbackMethod = "reserveInventoryFallback")
        @Bulkhead(name = "inventoryReserveBulkhead", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "reserveInventoryFallback")
        public Mono<InventoryReservationResponse> guardedReserveInventory(InventoryReservationRequest request) {
                return reserveInventory(request);
        }

        private Mono<InventoryAvailabilityResponse> checkAvailabilityFallback(String carId, Throwable throwable) {
                log.warn("Inventory availability degraded for car {}: {}", carId, throwable.getMessage());
                InventoryAvailabilityResponse fallback = new InventoryAvailabilityResponse();
                fallback.setCarId(carId);
                fallback.setAvailable(false);
                fallback.setErrorCode("SERVICE_UNAVAILABLE");
                fallback.setMessage("Inventory temporarily unavailable");
                return Mono.just(fallback);
        }

        private Mono<InventoryReservationResponse> reserveInventoryFallback(InventoryReservationRequest request,
                        Throwable throwable) {
                log.warn("Inventory reservation degraded for order {}: {}", request.getOrderId(), throwable.getMessage());
                InventoryReservationResponse fallback = new InventoryReservationResponse();
                fallback.setCarId(request.getCarId());
                fallback.setOrderId(request.getOrderId());
                fallback.setUnitsReserved(0);
                fallback.setUnitsRemaining(0);
                fallback.setStatus("FAILED");
                fallback.setErrorCode("SERVICE_UNAVAILABLE");
                fallback.setMessage("Inventory reservation temporarily unavailable");
                return Mono.just(fallback);
        }
}
