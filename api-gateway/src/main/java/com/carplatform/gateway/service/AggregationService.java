package com.carplatform.gateway.service;

import com.carplatform.gateway.dto.CarDetailsAggregatedResponse;
import com.carplatform.gateway.dto.CarListingAggregatedResponse;
import com.carplatform.gateway.client.CatalogServiceClient;
import com.carplatform.gateway.client.InventoryServiceClient;
import com.carplatform.gateway.dto.CarResponse;
import com.carplatform.gateway.dto.InventoryAvailabilityResponse;
import com.carplatform.gateway.exception.ResourceNotFoundException;
import com.carplatform.gateway.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Aggregation Service
 * 
 * Orchestrates calls to multiple microservices (Catalog, Inventory)
 * and merges their responses into a single aggregated response for clients.
 * 
 * Implements STEP 6.4-6.5: Aggregation Flow Design & Failure Handling
 * 
 * Phase 6: Aggregation Pattern
 */
@Slf4j
@Service
public class AggregationService {

    @Autowired
    private CatalogServiceClient catalogServiceClient;

    @Autowired
    private InventoryServiceClient inventoryServiceClient;

    // ===================== AGGREGATION: Car Details =====================

    /**
     * Get car details with availability (Aggregated API)
     * 
     * Flow:
     * 1. Fetch car from Catalog (mandatory)
     * 2. Fetch availability from Inventory (optional, with fallback)
     * 3. Merge and return
     * 
     * @param carId UUID of car
     * @return Aggregated response with car details + availability
     * @throws ResourceNotFoundException   if car not found in catalog
     * @throws ServiceUnavailableException if catalog service down
     */
    public CarDetailsAggregatedResponse getCarDetailsWithAvailability(UUID carId) {
        log.info("Aggregation: Fetching details for car: {}", carId);

        long startTime = System.currentTimeMillis();

        // STEP 1: Fetch from Catalog (REQUIRED)
        log.debug("Calling Catalog Service for car: {}", carId);
        CarResponse carDetails = catalogServiceClient.getCarById(carId);

        if (carDetails == null) {
            log.warn("Car not found in catalog: {}", carId);
            throw new ResourceNotFoundException("Car not found: " + carId);
        }

        log.debug("Catalog response received: {} {}", carDetails.getMake(), carDetails.getModel());

        // STEP 2: Fetch from Inventory (OPTIONAL, with fallback)
        CarDetailsAggregatedResponse.AvailabilityInfo availability;
        boolean partialResponse = false;
        try {
            log.debug("Calling Inventory Service for car: {}", carId);
            InventoryAvailabilityResponse inventoryResponse = inventoryServiceClient
                    .checkAvailability(carId.toString());

            if (inventoryResponse != null) {
                log.debug("Inventory response received: status={}", inventoryResponse.getStatus());
                availability = mapToAvailabilityInfo(inventoryResponse);
            } else {
                log.warn("Inventory returned null response for car: {}", carId);
                availability = CarDetailsAggregatedResponse.AvailabilityInfo
                        .unknown("Inventory service returned empty response");
                partialResponse = true;
            }
        } catch (ResourceNotFoundException e) {
            log.warn("Car not found in inventory (expected for new cars): {}", carId);
            availability = CarDetailsAggregatedResponse.AvailabilityInfo.outOfStock();
        } catch (Exception e) {
            log.warn("Inventory service unavailable, returning degraded response for car: {}", carId, e);
            availability = CarDetailsAggregatedResponse.AvailabilityInfo
                    .unknown("Inventory service temporarily unavailable");
            partialResponse = true;
        }

        CarDetailsAggregatedResponse.AggregationMetadata metadata = new CarDetailsAggregatedResponse.AggregationMetadata();
        if (partialResponse) {
            metadata.setAggregationStatus(206);
        }

        // STEP 3: Merge and return
        CarDetailsAggregatedResponse response = CarDetailsAggregatedResponse.builder()
                .carId(carDetails.getId())
                .make(carDetails.getMake())
                .model(carDetails.getModel())
                .year(carDetails.getYear())
                .price(carDetails.getPrice())
                .color(carDetails.getColor())
                .availability(availability)
                .metadata(metadata)
                .build();

        long duration = System.currentTimeMillis() - startTime;
        log.info("Aggregation complete for car {}: {}ms, availability: {}",
                carId, duration, availability.getStatus());

        return response;
    }

    // ===================== AGGREGATION: Car Listing =====================

    /**
     * Get car listing with availability flags (Aggregated API)
     * 
     * Flow:
     * 1. Fetch all cars from Catalog
     * 2. For each car, fetch availability from Inventory
     * 3. Merge and return
     * 
     * @param page Page number (1-indexed)
     * @param size Page size (max 100)
     * @return Aggregated response with car list + availability
     * @throws ServiceUnavailableException if catalog service down
     */
    public CarListingAggregatedResponse getCarListingWithAvailability(int page, int size) {
        log.info("Aggregation: Fetching car listing (page={}, size={})", page, size);

        // Validate pagination
        if (size > 100) {
            log.warn("Page size {} exceeds maximum 100, capping to 100", size);
            size = 100;
        }
        if (page < 1) {
            page = 1;
        }

        long startTime = System.currentTimeMillis();

        // STEP 1: Fetch all cars from Catalog
        log.debug("Calling Catalog Service for car listing");
        List<CarResponse> allCars = catalogServiceClient.listAllCars();

        if (allCars == null || allCars.isEmpty()) {
            log.debug("No cars found in catalog");
            return new CarListingAggregatedResponse(new ArrayList<>(), 0, size, page);
        }

        log.debug("Catalog returned {} cars", allCars.size());

        // STEP 2: Paginate
        int totalCount = allCars.size();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, totalCount);

        List<CarResponse> pageItems = allCars.subList(startIndex, endIndex);
        log.debug("Paginated: {} items on page {} of {}", pageItems.size(), page, totalPages);

        // STEP 3: Fetch availability for each car (with fallback)
        List<CarListingAggregatedResponse.CarListItem> listItems = pageItems.stream()
                .map(car -> mapToCarListItem(car))
                .collect(Collectors.toList());

        // STEP 4: Return
        CarListingAggregatedResponse response = new CarListingAggregatedResponse(
                listItems,
                totalCount,
                size,
                page);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Aggregation complete for car listing: {}ms, {} items", duration, listItems.size());

        return response;
    }

    // ===================== Helper: Map Inventory Response to Availability
    // =====================

    /**
     * Convert InventoryAvailabilityResponse to
     * CarDetailsAggregatedResponse.AvailabilityInfo
     */
    private CarDetailsAggregatedResponse.AvailabilityInfo mapToAvailabilityInfo(
            InventoryAvailabilityResponse inventoryResponse) {

        if (inventoryResponse == null) {
            return CarDetailsAggregatedResponse.AvailabilityInfo.unknown("Null inventory response");
        }

        int total = inventoryResponse.getTotalUnits() != null ? inventoryResponse.getTotalUnits() : 0;
        int reserved = inventoryResponse.getReservedUnits() != null ? inventoryResponse.getReservedUnits() : 0;
        int available = inventoryResponse.getAvailableUnits() != null ? inventoryResponse.getAvailableUnits() : 0;

        // Determine status
        if (available > 0) {
            return CarDetailsAggregatedResponse.AvailabilityInfo
                    .inStock(total, available, reserved);
        } else {
            return CarDetailsAggregatedResponse.AvailabilityInfo.outOfStock();
        }
    }

    // ===================== Helper: Map Car to ListItem =====================

    /**
     * Convert CarResponse to CarListingAggregatedResponse.CarListItem with
     * availability
     */
    private CarListingAggregatedResponse.CarListItem mapToCarListItem(CarResponse car) {

        String availabilityStatus = "UNKNOWN";
        Integer availableUnits = null;

        try {
            // Try to fetch availability (with timeout/retry handled by client)
            InventoryAvailabilityResponse invResponse = inventoryServiceClient
                    .checkAvailability(car.getId().toString());

            if (invResponse != null) {
                if (invResponse.getAvailableUnits() != null && invResponse.getAvailableUnits() > 0) {
                    availabilityStatus = "IN_STOCK";
                    availableUnits = invResponse.getAvailableUnits();
                } else {
                    availabilityStatus = "OUT_OF_STOCK";
                    availableUnits = 0;
                }
            }
        } catch (Exception e) {
            log.debug("Could not fetch availability for car {}, using UNKNOWN status", car.getId(), e);
            availabilityStatus = "UNKNOWN";
        }

        return new CarListingAggregatedResponse.CarListItem(
                car.getId(),
                car.getMake(),
                car.getModel(),
                car.getYear(),
                car.getPrice().doubleValue(),
                availabilityStatus,
                availableUnits);
    }
}
