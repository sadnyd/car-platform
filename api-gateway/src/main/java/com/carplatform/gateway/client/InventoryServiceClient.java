package com.carplatform.gateway.client;

import com.carplatform.gateway.dto.InventoryAvailabilityResponse;
import com.carplatform.gateway.exception.ResourceNotFoundException;
import com.carplatform.gateway.exception.ServiceUnavailableException;
import com.carplatform.gateway.util.TraceIdManager;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 */
@Slf4j
@Component
public class InventoryServiceClient {

    private final WebClient webClient;

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    public InventoryServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${external.services.inventory-url:http://localhost:8082}") String inventoryServiceUrl) {
        this.webClient = webClientBuilder
                .filter((request, next) -> next.exchange(
                        org.springframework.web.reactive.function.client.ClientRequest.from(request)
                                .headers(headers -> {
                                    String traceId = TraceIdManager.get();
                                    if (!traceId.isBlank()) {
                                        headers.set(TraceIdManager.getHeaderName(), traceId);
                                        headers.set(TraceIdManager.getCorrelationHeaderName(), traceId);
                                    }
                                })
                                .build()))
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    // ===================== CHECK AVAILABILITY =====================

    /**
     * Check availability for a car
     * 
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
        Timer.Sample sample = meterRegistry != null ? Timer.start(meterRegistry) : null;
        try {
            InventoryAvailabilityResponse response = webClient
                    .get()
                    .uri("/inventory/check-availability/{carId}", carId)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            clientResponse -> reactor.core.publisher.Mono.error(
                                    new ResourceNotFoundException("Car not found in inventory: " + carId)))
                    .bodyToMono(InventoryAvailabilityResponse.class)
                    .timeout(Duration.ofSeconds(2))
                    .block();

            if (sample != null) {
                sample.stop(meterRegistry.timer("carplatform.gateway.downstream.inventory.latency", "operation",
                        "checkAvailability"));
            }
            return response;
        } catch (WebClientResponseException exception) {
            if (meterRegistry != null) {
                meterRegistry
                        .counter("carplatform.gateway.downstream.inventory.errors", "operation", "checkAvailability")
                        .increment();
            }
            if (exception.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Car not found in inventory: " + carId);
            }
            throw new ServiceUnavailableException("Inventory service temporarily unavailable", exception);
        } catch (Exception exception) {
            if (meterRegistry != null) {
                meterRegistry
                        .counter("carplatform.gateway.downstream.inventory.errors", "operation", "checkAvailability")
                        .increment();
            }
            throw new ServiceUnavailableException("Inventory service temporarily unavailable", exception);
        }
    }

    @CircuitBreaker(name = "inventoryServiceCircuitBreaker", fallbackMethod = "checkAvailabilityFallback")
    @Retry(name = "inventoryServiceRetry", fallbackMethod = "checkAvailabilityFallback")
    @Bulkhead(name = "inventoryServiceBulkhead", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "checkAvailabilityFallback")
    public InventoryAvailabilityResponse guardedCheckAvailability(String carId) {
        return checkAvailability(carId);
    }

    private InventoryAvailabilityResponse checkAvailabilityFallback(String carId, Throwable throwable) {
        log.warn("Inventory fallback for car {} due to {}", carId, throwable.getMessage());
        InventoryAvailabilityResponse fallback = new InventoryAvailabilityResponse();
        fallback.setStatus("UNKNOWN");
        fallback.setAvailableUnits(0);
        fallback.setReservedUnits(0);
        fallback.setTotalUnits(0);
        return fallback;
    }
}
