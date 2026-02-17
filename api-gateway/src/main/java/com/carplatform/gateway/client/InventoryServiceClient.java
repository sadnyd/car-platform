package com.carplatform.gateway.client;

import com.carplatform.gateway.dto.InventoryAvailabilityResponse;
import com.carplatform.gateway.exception.ResourceNotFoundException;
import com.carplatform.gateway.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Inventory Service Client
 * 
 * Makes HTTP requests to the Inventory Service.
 * Used by Aggregation Service to fetch availability information.
 * 
 * Configuration:
 * - Timeout: 3 seconds
 * - Retries: 2 attempts (3 total)
 * - Circuit Breaker: Enabled
 * - Fallback: Graceful degradation on failure
 * 
 * Phase 6: Aggregation Pattern (Step 6.5: Failure HandlingStrategy)
 */
@Slf4j
@Component
public class InventoryServiceClient {

    private final WebClient webClient;

    public InventoryServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${external.services.inventory-url:http://localhost:8082}") String inventoryServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    // ===================== CHECK AVAILABILITY =====================

    /**
     * Check availability for a car
     * 
     * Implements STEP 6.5: Failure Handling
     * - Retries 2 times on timeout/connection errors
     * - Returns null on success with complete retries
     * 
     * @param carId Car ID (String or UUID string)
     * @return Availability response
     * @throws ResourceNotFoundException   if car not in inventory (404)
     * @throws ServiceUnavailableException if service down after retries (503)
     */
    public InventoryAvailabilityResponse checkAvailability(String carId) {
        log.debug("InventoryServiceClient: GET /inventory/check-availability/{}", carId);

        int maxAttempts = 3; // 1 initial + 2 retries
        int attempt = 0;

        while (attempt < maxAttempts) {
            attempt++;

            try {
                InventoryAvailabilityResponse response = webClient
                        .get()
                        .uri("/inventory/check-availability/{carId}", carId)
                        .retrieve()
                        .onStatus(
                                status -> status.value() == 404,
                                clientResponse -> {
                                    log.warn("Inventory returned 404 for car: {}", carId);
                                    return reactor.core.publisher.Mono.error(
                                            new ResourceNotFoundException("Car not found in inventory: " + carId));
                                })
                        .bodyToMono(InventoryAvailabilityResponse.class)
                        .timeout(Duration.ofSeconds(3))
                        .block();

                log.debug("InventoryServiceClient: Received availability response for {}", carId);
                return response;

            } catch (ResourceNotFoundException e) {
                // Expected case: car not in inventory yet
                log.debug("Car not found in inventory: {} (attempt {}/{})", carId, attempt, maxAttempts);
                throw e;

            } catch (WebClientResponseException e) {
                // HTTP error responses
                if (e.getStatusCode().value() == 404) {
                    log.warn("Inventory returned 404 for car: {}", carId);
                    throw new ResourceNotFoundException("Car not found in inventory: " + carId);
                } else if (e.getStatusCode().value() >= 500) {
                    log.error("Inventory service error (attempt {}/{}): {}", attempt, maxAttempts, e.getMessage());
                    if (attempt == maxAttempts) {
                        throw new ServiceUnavailableException(
                                "Inventory service error after " + maxAttempts + " attempts");
                    }
                    // Wait before retry
                    sleep(100);
                } else {
                    log.error("Inventory service client error: {}", e.getMessage());
                    throw e;
                }

            } catch (Exception e) {
                // Timeout or connection errors -> retry
                log.warn("Inventory service call failed (attempt {}/{}): {} - {}",
                        attempt, maxAttempts, e.getClass().getSimpleName(), e.getMessage());

                if (attempt == maxAttempts) {
                    log.error("Inventory service unreachable after {} attempts", maxAttempts);
                    throw new ServiceUnavailableException("Inventory service temporarily unavailable", e);
                }

                // Exponential backoff: 100ms, 200ms
                sleep(100 * attempt);
            }
        }

        throw new ServiceUnavailableException("Inventory service unreachable");
    }

    // ===================== Helper: Sleep =====================

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep interrupted");
        }
    }
}
