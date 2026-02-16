package com.carplatform.gateway.client;

import com.carplatform.gateway.dto.CarResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

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
 * Phase 6: Aggregation Pattern
 */
@Slf4j
@Component
public class CatalogServiceClient {

    @Autowired
    private WebClient webClient;

    @Value("${external.services.catalog-url:http://localhost:8081}")
    private String catalogServiceUrl;

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

        try {
            return webClient
                    .get()
                    .uri(catalogServiceUrl + "/catalog/{id}", carId)
                    .retrieve()
                    .bodyToMono(CarResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block(); // Blocking for synchronous gateway

        } catch (Exception e) {
            log.error("Error calling Catalog Service for car {}: {}", carId, e.getMessage());
            throw new RuntimeException("Failed to fetch car from catalog", e);
        }
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

        try {
            CarResponse[] carArray = webClient
                    .get()
                    .uri(catalogServiceUrl + "/catalog")
                    .retrieve()
                    .bodyToMono(CarResponse[].class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            List<CarResponse> cars = carArray != null ? java.util.Arrays.asList(carArray)
                    : java.util.Collections.emptyList();

            log.debug("CatalogServiceClient: Listed {} cars", cars.size());
            return cars;

        } catch (Exception e) {
            log.error("Error calling Catalog Service for listing: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch car listing from catalog", e);
        }
    }
}
