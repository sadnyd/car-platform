package com.carplatform.inventory.dto;

import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
        UUID inventoryId,
        UUID carId,
        int availableUnits,
        int reservedUnits,
        String location,
        Instant lastUpdated) {
}
