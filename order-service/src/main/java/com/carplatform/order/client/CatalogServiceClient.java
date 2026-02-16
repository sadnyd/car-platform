package com.carplatform.order.client;

import com.carplatform.order.dto.CarDetailsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Catalog Service Client
 * 
 * Read-only access to catalog information
 * - Fetch car details for order confirmation
 * - No state changes
 */
@Slf4j
@Component
public class CatalogServiceClient {

    private final WebClient webClient;
    private final String catalogBaseUrl;

    public CatalogServiceClient(WebClient webClient,
            @Value("${services.catalog.base-url:http://localhost:8081}") String catalogBaseUrl) {
        this.webClient = webClient;
        this.catalogBaseUrl = catalogBaseUrl;
    }

    /**
     * Fetch car details from catalog
     * 
     * @param carId Car ID to fetch
     * @return Mono<CarDetailsResponse> with car information
     * 
     *         Error handling:
     *         - Timeout: Fails with TimeoutException
     *         - Service down: Fails with connection error
     *         - Car not found: Returns 404
     */
    public Mono<CarDetailsResponse> getCarDetails(String carId) {
        log.debug("Fetching car details from catalog - carId: {}", carId);

        return webClient.get()
                .uri(catalogBaseUrl + "/catalog/cars/{carId}", carId)
                .retrieve()
                .bodyToMono(CarDetailsResponse.class)
                .timeout(Duration.ofSeconds(3)) // Shorter timeout for read-only
                .doOnSuccess(response -> log.info("Car details fetched - car: {} {}", response.getBrand(),
                        response.getModel()))
                .doOnError(error -> log.error("Failed to fetch car details for {}: {}", carId, error.getMessage()));
    }
}
