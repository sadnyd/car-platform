package com.carplatform.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for availability check endpoint (Phase 5.3)
 * 
 * Used by: Order Service (inter-service call)
 * Endpoint: GET /inventory/check-availability/{carId}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityCheckResponse {

    private String carId;
    private boolean available;
    private int totalUnits;
    private int reservedUnits;
    private int availableUnits;
    private String errorCode;
    private String message;

    /**
     * Factory method for success case
     */
    public static AvailabilityCheckResponse success(String carId, int totalUnits, int reservedUnits,
            int availableUnits) {
        AvailabilityCheckResponse response = new AvailabilityCheckResponse();
        response.setCarId(carId);
        response.setAvailable(availableUnits > 0);
        response.setTotalUnits(totalUnits);
        response.setReservedUnits(reservedUnits);
        response.setAvailableUnits(availableUnits);
        return response;
    }

    /**
     * Factory method for not found case
     */
    public static AvailabilityCheckResponse notFound(String carId) {
        AvailabilityCheckResponse response = new AvailabilityCheckResponse();
        response.setCarId(carId);
        response.setAvailable(false);
        response.setErrorCode("RESOURCE_NOT_FOUND");
        response.setMessage("Car not found in inventory: " + carId);
        return response;
    }

    /**
     * Factory method for out of stock case
     */
    public static AvailabilityCheckResponse outOfStock(String carId, int totalUnits) {
        AvailabilityCheckResponse response = new AvailabilityCheckResponse();
        response.setCarId(carId);
        response.setAvailable(false);
        response.setTotalUnits(totalUnits);
        response.setReservedUnits(totalUnits);
        response.setAvailableUnits(0);
        response.setErrorCode("INSUFFICIENT_STOCK");
        response.setMessage("Insufficient stock for car: " + carId);
        return response;
    }
}
