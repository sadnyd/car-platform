package com.carplatform.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.UUID;

/**
 * Inventory Availability Response DTO
 * 
 * Represents inventory availability information as returned from the Inventory
 * Service.
 * This is a mapping DTO used internally in the Gateway during aggregation.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryAvailabilityResponse implements Serializable {

    private UUID carId;
    private String status; // IN_STOCK, OUT_OF_STOCK, UNKNOWN
    private Integer totalUnits;
    private Integer availableUnits;
    private Integer reservedUnits;

    // ===================== Constructors =====================

    public InventoryAvailabilityResponse() {
    }

    public InventoryAvailabilityResponse(UUID carId, String status) {
        this.carId = carId;
        this.status = status;
    }

    public InventoryAvailabilityResponse(
            UUID carId,
            String status,
            Integer totalUnits,
            Integer availableUnits,
            Integer reservedUnits) {
        this.carId = carId;
        this.status = status;
        this.totalUnits = totalUnits;
        this.availableUnits = availableUnits;
        this.reservedUnits = reservedUnits;
    }

    // ===================== Getters & Setters =====================

    public UUID getCarId() {
        return carId;
    }

    public void setCarId(UUID carId) {
        this.carId = carId;
    }

    public void setCarId(String carId) {
        this.carId = carId != null ? UUID.fromString(carId) : null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(Integer totalUnits) {
        this.totalUnits = totalUnits;
    }

    public Integer getAvailableUnits() {
        return availableUnits;
    }

    public void setAvailableUnits(Integer availableUnits) {
        this.availableUnits = availableUnits;
    }

    public Integer getReservedUnits() {
        return reservedUnits;
    }

    public void setReservedUnits(Integer reservedUnits) {
        this.reservedUnits = reservedUnits;
    }

    @Override
    public String toString() {
        return "InventoryAvailabilityResponse{" +
                "carId=" + carId +
                ", status='" + status + '\'' +
                ", totalUnits=" + totalUnits +
                ", availableUnits=" + availableUnits +
                ", reservedUnits=" + reservedUnits +
                '}';
    }
}
