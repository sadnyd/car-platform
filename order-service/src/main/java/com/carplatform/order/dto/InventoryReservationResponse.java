package com.carplatform.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response from Inventory Service - Reservation Confirmation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationResponse {

    private String reservationId;
    private String carId;
    private String orderId;
    private int unitsReserved;
    private int unitsRemaining;
    private Instant reservationExpiry;
    private String status;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("message")
    private String message;

    /**
     * Factory method for successful reservation
     */
    public static InventoryReservationResponse success(UUID reservationId, UUID carId, String orderId,
            int unitsReserved, int unitsRemaining) {
        InventoryReservationResponse response = new InventoryReservationResponse();
        response.setReservationId(reservationId.toString());
        response.setCarId(carId.toString());
        response.setOrderId(orderId);
        response.setUnitsReserved(unitsReserved);
        response.setUnitsRemaining(unitsRemaining);
        response.setReservationExpiry(Instant.now().plusSeconds(86400)); // 1 day
        response.setStatus("RESERVED");
        return response;
    }

    /**
     * Factory method for insufficient stock
     */
    public static InventoryReservationResponse insufficientStock(String carId, String orderId) {
        InventoryReservationResponse response = new InventoryReservationResponse();
        response.setCarId(carId);
        response.setOrderId(orderId);
        response.setUnitsReserved(0);
        response.setUnitsRemaining(0);
        response.setStatus("FAILED");
        response.setErrorCode("INSUFFICIENT_STOCK");
        response.setMessage("Not enough units available for car: " + carId);
        return response;
    }
}
