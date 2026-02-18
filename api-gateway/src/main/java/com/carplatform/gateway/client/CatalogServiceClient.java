package com.carplatform.gateway.client;

import com.carplatform.gateway.dto.CarResponse;
import com.carplatform.gateway.util.TraceIdManager;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Catalog Service Client
 * 
 * Makes HTTP requests to the Catalog Service.
 * Used by Aggregation Service to fetch car details.
 * 
 * Configuration:
 * - Timeout: 5 seconds
 * - Retries: None (prefer fast fail)
 * - Circuit Breaker: Enabled
 * 
 * Aggregation Pattern
 */
@Slf4j
@Component
public class CatalogServiceClient {

    private final WebClient webClient;

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    public CatalogServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${external.services.catalog-url:http://localhost:8081}") String catalogServiceUrl) {
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
                .baseUrl(catalogServiceUrl)
                .build();
    }

    // ===================== GET CAR BY ID =====================

    /**
     * Get a single car by ID
     * 
     * @param carId UUID of car
     * @return Car response
     * @throws ResourceNotFoundException   if car not found
     * @throws ServiceUnavailableException if service unreachable
     */
    public CarResponse getCarById(UUID carId) {
        log.debug("CatalogServiceClient: GET /catalog/{}", carId);
        Timer.Sample sample = meterRegistry != null ? Timer.start(meterRegistry) : null;

        try {
            CarResponse response = webClient
                    .get()
                    .uri("/catalog/{id}", carId)
                    .retrieve()
                    .bodyToMono(CarResponse.class)
                    .timeout(Duration.ofSeconds(3))
                    .block(); // Blocking for synchronous gateway
            if (sample != null) {
                sample.stop(meterRegistry.timer("carplatform.gateway.downstream.catalog.latency", "operation",
                        "getCarById"));
            }
            return response;

        } catch (Exception e) {
            if (meterRegistry != null) {
                meterRegistry.counter("carplatform.gateway.downstream.catalog.errors", "operation", "getCarById")
                        .increment();
            }
            log.error("Error calling Catalog Service for car {}: {}", carId, e.getMessage());
            throw new RuntimeException("Failed to fetch car from catalog", e);
        }
    }

    @CircuitBreaker(name = "catalogServiceCircuitBreaker", fallbackMethod = "getCarByIdFallback")
    @Bulkhead(name = "catalogServiceBulkhead", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "getCarByIdFallback")
    public CarResponse guardedGetCarById(UUID carId) {
        return getCarById(carId);
    }

    // ===================== LIST ALL CARS =====================

    /**
     * Get all cars from catalog
     * 
     * @return List of cars
     * @throws ServiceUnavailableException if service unreachable
     */
    public List<CarResponse> listAllCars() {
        log.debug("CatalogServiceClient: GET /catalog");
        Timer.Sample sample = meterRegistry != null ? Timer.start(meterRegistry) : null;

        try {
            CarResponse[] carArray = webClient
                    .get()
                    .uri("/catalog")
                    .retrieve()
                    .bodyToMono(CarResponse[].class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            List<CarResponse> cars = carArray != null ? java.util.Arrays.asList(carArray)
                    : java.util.Collections.emptyList();

            if (sample != null) {
                sample.stop(meterRegistry.timer("carplatform.gateway.downstream.catalog.latency", "operation",
                        "listAllCars"));
            }

            log.debug("CatalogServiceClient: Listed {} cars", cars.size());
            return cars;

        } catch (Exception e) {
            if (meterRegistry != null) {
                meterRegistry.counter("carplatform.gateway.downstream.catalog.errors", "operation", "listAllCars")
                        .increment();
            }
            log.error("Error calling Catalog Service for listing: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch car listing from catalog", e);
        }
    }

    @CircuitBreaker(name = "catalogServiceCircuitBreaker", fallbackMethod = "listAllCarsFallback")
    @Bulkhead(name = "catalogServiceBulkhead", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "listAllCarsFallback")
    public List<CarResponse> guardedListAllCars() {
        return listAllCars();
    }

    private CarResponse getCarByIdFallback(UUID carId, Throwable throwable) {
        log.warn("Catalog fallback for car {} due to {}", carId, throwable.getMessage());
        CarResponse fallback = new CarResponse();
        fallback.setId(carId);
        fallback.setModel("UNKNOWN");
        fallback.setPrice(0.0);
        return fallback;
    }

    private List<CarResponse> listAllCarsFallback(Throwable throwable) {
        log.warn("Catalog list fallback due to {}", throwable.getMessage());
        return java.util.Collections.emptyList();
    }
}
