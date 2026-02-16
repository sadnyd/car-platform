package com.carplatform.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request for inventory reservation endpoint (Phase 5.4)
 * 
 * Used by: Order Service (inter-service call)
 * Endpoint: POST /inventory/reserve
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

    private String carId;
    private String orderId;
    private int units;
}
