package com.carplatform.gateway.controller;

import com.carplatform.gateway.dto.CarDetailsAggregatedResponse;
import com.carplatform.gateway.dto.CarListingAggregatedResponse;
import com.carplatform.gateway.service.AggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

/**
 * Aggregation API Controller
 * 
 * Exposes client-friendly aggregated endpoints that combine data from multiple
 * services.
 * 
 * These are NEW endpoints at the Gateway level, not proxies to downstream
 * services.
 * 
 * STEP 6.4: Aggregation Flow Design
 * STEP 6.9: API Documentation
 * 
 * Phase 6: Aggregation Pattern
 */
@Slf4j
@RestController
@RequestMapping("/api/cars")
@Tag(name = "Car Aggregation APIs", description = "Client-friendly aggregated car endpoints")
public class AggregationController {

    @Autowired
    private AggregationService aggregationService;

    // ===================== ENDPOINT 1: Car Details with Availability
    // =====================

    /**
     * Get car details with real-time availability
     * 
     * Aggregates data from:
     * - Catalog Service (car details)
     * - Inventory Service (availability status)
     * 
     * @param carId UUID of the car
     * @return Aggregated car details with availability
     */
    @GetMapping("/{carId}/details")
    @Operation(summary = "Get car details with availability", description = "Returns aggregated car information including real-time availability status. "
            +
            "Combines data from Catalog Service (car details) and Inventory Service (availability). " +
            "If inventory service is temporarily unavailable, returns car details with UNKNOWN availability status (HTTP 206).", tags = {
                    "Cars" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Car details retrieved successfully with complete availability data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarDetailsAggregatedResponse.class), examples = @ExampleObject(value = "{\"carId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"make\":\"Tesla\",\"model\":\"Model S\",\"year\":2024,\"price\":89999.99,\"color\":\"Black\",\"availability\":{\"status\":\"IN_STOCK\",\"totalUnits\":10,\"availableUnits\":8,\"reservedUnits\":2},\"metadata\":{\"aggregationStatus\":200,\"aggregatedAt\":\"2026-02-16T16:45:00Z\"}}"))),
            @ApiResponse(responseCode = "206", description = "Partial aggregation - car found but inventory service temporarily unavailable (degraded response)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarDetailsAggregatedResponse.class), examples = @ExampleObject(value = "{\"carId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"make\":\"Tesla\",\"model\":\"Model S\",\"year\":2024,\"price\":89999.99,\"color\":\"Black\",\"availability\":{\"status\":\"UNKNOWN\",\"reason\":\"Inventory service temporarily unavailable\"},\"metadata\":{\"aggregationStatus\":206}}"))),
            @ApiResponse(responseCode = "404", description = "Car not found in catalog service", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\":\"Car not found\",\"carId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\"}"))),
            @ApiResponse(responseCode = "503", description = "Catalog service unavailable - cannot retrieve car details", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\":\"Service unavailable\",\"service\":\"catalog\",\"message\":\"Catalog service is temporarily unavailable\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid car ID format (must be valid UUID)")
    })
    public Mono<ResponseEntity<CarDetailsAggregatedResponse>> getCarDetails(
            @Parameter(name = "carId", description = "UUID of the car to retrieve", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable UUID carId) {

        log.info("API Request: GET /api/cars/{}/details", carId);

        return Mono.fromCallable(() -> aggregationService.getCarDetailsWithAvailability(carId))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> {
                    log.debug("API Response 200: Car details retrieved for {}", carId);
                    return ResponseEntity.ok(response);
                })
                .doOnError(e -> log.error("API Error for car details: {}", carId, e));
    }

    // ===================== ENDPOINT 2: Car Listing with Availability
    // =====================

    /**
     * Get car listing with real-time availability flags
     * 
     * Aggregates data from:
     * - Catalog Service (all cars)
     * - Inventory Service (availability per car)
     * 
     * @param page Page number (default 1)
     * @param size Page size (default 20, max 100)
     * @return Paginated list of cars with availability
     */
    @GetMapping("/listing")
    @Operation(summary = "Get paginated car listing with availability", description = "Returns a paginated list of all cars with their real-time availability status. "
            +
            "Combines data from Catalog Service (all cars) and Inventory Service (per-car availability). " +
            "Supports pagination with configurable page size (max 100). " +
            "If inventory service is unavailable for specific cars, shows UNKNOWN availability for those items.", tags = {
                    "Cars" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Car listing retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarListingAggregatedResponse.class), examples = @ExampleObject(value = "{\"cars\":[{\"carId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"make\":\"Tesla\",\"model\":\"Model S\",\"year\":2024,\"price\":89999.99,\"availabilityStatus\":\"IN_STOCK\",\"availableUnits\":8},{\"carId\":\"550e8400-e29b-41d4-a716-446655440000\",\"make\":\"BMW\",\"model\":\"X5\",\"year\":2024,\"price\":65000.00,\"availabilityStatus\":\"OUT_OF_STOCK\",\"availableUnits\":0}],\"pagination\":{\"totalCount\":50,\"pageSize\":20,\"currentPage\":1,\"totalPages\":3},\"metadata\":{\"aggregationStatus\":200,\"aggregatedAt\":\"2026-02-16T16:45:00Z\"}}"))),
            @ApiResponse(responseCode = "503", description = "Catalog service unavailable - cannot retrieve car list", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\":\"Service unavailable\",\"service\":\"catalog\",\"message\":\"Catalog service is temporarily unavailable\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters (page < 1 or size < 1 or size > 100)")
    })
    public Mono<ResponseEntity<CarListingAggregatedResponse>> getCarListing(
            @Parameter(name = "page", description = "Page number (1-indexed)", example = "1", required = false) @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(name = "size", description = "Page size (1-100, default 20)", example = "20", required = false) @RequestParam(value = "size", defaultValue = "20") int size) {

        log.info("API Request: GET /api/cars/listing?page={}&size={}", page, size);

        // Validate pagination
        if (page < 1) {
            log.warn("Invalid page number: {}", page);
            return Mono.just(ResponseEntity.badRequest().build());
        }
        if (size < 1 || size > 100) {
            log.warn("Invalid page size: {} (must be 1-100)", size);
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return Mono.fromCallable(() -> aggregationService.getCarListingWithAvailability(page, size))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> {
                    log.debug("API Response 200: Car listing retrieved, {} items", response.getCars().size());
                    return ResponseEntity.ok(response);
                })
                .doOnError(e -> log.error("API Error for car listing", e));
    }

    // ===================== HEALTH CHECK =====================

    /**
     * Health check for aggregation API
     */
    @GetMapping("/aggregation/health")
    @Operation(summary = "Health check for aggregation service", description = "Returns health status of the aggregation API. Used for load balancer and monitoring health checks.", tags = {
            "Health" }, hidden = false)
    @ApiResponse(responseCode = "200", description = "Aggregation API is healthy")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(java.util.Map.of(
                "status", "UP",
                "service", "aggregation-api",
                "timestamp", java.time.Instant.now()));
    }
}
