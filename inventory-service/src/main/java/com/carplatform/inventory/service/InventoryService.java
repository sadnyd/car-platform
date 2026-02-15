package com.carplatform.inventory.service;

import com.carplatform.inventory.dto.InventoryResponse;
import com.carplatform.inventory.dto.CreateInventoryRequest;
import com.carplatform.inventory.dto.UpdateInventoryRequest;
import com.carplatform.inventory.dto.ReserveInventoryRequest;
import com.carplatform.inventory.dto.ReleaseInventoryRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inventory Service Interface
 * 
 * Defines the contract for inventory management operations.
 * Handles stock tracking, reservations, and availability checks.
 */
public interface InventoryService {

    /**
     * Create inventory for a car
     */
    InventoryResponse createInventory(CreateInventoryRequest request);

    /**
     * Get inventory by ID
     */
    Optional<InventoryResponse> getInventoryById(UUID inventoryId);

    /**
     * Get inventory by car ID
     */
    Optional<InventoryResponse> getInventoryByCarId(UUID carId);

    /**
     * List all inventory
     */
    List<InventoryResponse> listAllInventory();

    /**
     * Update inventory (units only)
     */
    InventoryResponse updateInventory(UUID inventoryId, UpdateInventoryRequest request);

    /**
     * Reserve units from inventory
     */
    InventoryResponse reserveInventory(UUID inventoryId, ReserveInventoryRequest request);

    /**
     * Release reserved units back to available
     */
    InventoryResponse releaseInventory(UUID inventoryId, ReleaseInventoryRequest request);

    /**
     * Check availability
     */
    boolean isAvailable(UUID carId, int requiredUnits);
}
