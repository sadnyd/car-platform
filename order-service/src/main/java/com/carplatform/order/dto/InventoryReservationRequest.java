package com.carplatform.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to Inventory Service - Reserve Stock
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationRequest {

    private String carId;
    private String orderId;
    private int units;
}
