package com.carplatform.inventory.model;

import java.time.Instant;
import java.util.UUID;

public record Inventory(
        UUID inventoryId,
        UUID carId,
        int availableUnits,
        int reservedUnits,
        String location,
        Instant lastUpdated) {
}
