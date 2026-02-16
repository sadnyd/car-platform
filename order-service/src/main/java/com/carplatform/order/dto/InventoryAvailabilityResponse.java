package com.carplatform.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from Inventory Service - Availability Check
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAvailabilityResponse {

    private String carId;
    private boolean available;
    private int totalUnits;
    private int reservedUnits;
    private int availableUnits;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("message")
    private String message;

    /**
     * Factory method for success case
     */
    public static InventoryAvailabilityResponse success(String carId, int totalUnits, int reservedUnits,
            int availableUnits) {
        InventoryAvailabilityResponse response = new InventoryAvailabilityResponse();
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
    public static InventoryAvailabilityResponse notFound(String carId) {
        InventoryAvailabilityResponse response = new InventoryAvailabilityResponse();
        response.setCarId(carId);
        response.setAvailable(false);
        response.setErrorCode("RESOURCE_NOT_FOUND");
        response.setMessage("Car not found in inventory: " + carId);
        return response;
    }

    /**
     * Factory method for out of stock case
     */
    public static InventoryAvailabilityResponse outOfStock(String carId, int totalUnits) {
        InventoryAvailabilityResponse response = new InventoryAvailabilityResponse();
        response.setCarId(carId);
        response.setAvailable(false);
        response.setTotalUnits(totalUnits);
        response.setReservedUnits(totalUnits);
        response.setAvailableUnits(0);
        response.setErrorCode("INSUFFICIENT_STOCK");
        response.setMessage("Insufficient inventory for car: " + carId);
        return response;
    }
}
