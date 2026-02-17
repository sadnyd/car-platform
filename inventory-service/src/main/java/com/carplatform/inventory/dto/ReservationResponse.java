package com.carplatform.inventory.dto;

// import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response for inventory reservation endpoint (Phase 5.4)
 * 
 * Used by: Order Service (inter-service call)
 * Endpoint: POST /inventory/reserve
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private String reservationId;
    private String carId;
    private String orderId;
    private int unitsReserved;
    private int unitsRemaining;
    private Instant reservationExpiry;
    private String status;
    private String errorCode;
    private String message;

    /**
     * Factory method for successful reservation
     */
    public static ReservationResponse success(UUID reservationId, UUID carId, String orderId,
            int unitsReserved, int unitsRemaining) {
        ReservationResponse response = new ReservationResponse();
        response.setReservationId(reservationId.toString());
        response.setCarId(carId.toString());
        response.setOrderId(orderId);
        response.setUnitsReserved(unitsReserved);
        response.setUnitsRemaining(unitsRemaining);
        response.setReservationExpiry(Instant.now().plusSeconds(24 * 60 * 60)); // 24 hours
        response.setStatus("ACTIVE");
        return response;
    }

    /**
     * Factory method for reservation failure (insufficient stock)
     */
    public static ReservationResponse insufficientStock(String carId, String orderId) {
        ReservationResponse response = new ReservationResponse();
        response.setCarId(carId);
        response.setOrderId(orderId);
        response.setErrorCode("RESERVATION_FAILED");
        response.setMessage("Cannot reserve: insufficient stock after concurrent updates");
        return response;
    }

    /**
     * Factory method for car not found
     */
    public static ReservationResponse carNotFound(String carId) {
        ReservationResponse response = new ReservationResponse();
        response.setCarId(carId);
        response.setErrorCode("RESOURCE_NOT_FOUND");
        response.setMessage("Car not found in inventory: " + carId);
        return response;
    }
}
