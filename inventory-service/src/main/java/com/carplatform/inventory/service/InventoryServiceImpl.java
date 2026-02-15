package com.carplatform.inventory.service;

import com.carplatform.inventory.dto.InventoryResponse;
import com.carplatform.inventory.dto.CreateInventoryRequest;
import com.carplatform.inventory.dto.UpdateInventoryRequest;
import com.carplatform.inventory.dto.ReserveInventoryRequest;
import com.carplatform.inventory.dto.ReleaseInventoryRequest;
import com.carplatform.inventory.model.Inventory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-Memory Implementation of Inventory Service
 * 
 * Uses a HashMap to store inventory for Phase 3 (no database).
 * Business Logic: availableUnits + reservedUnits <= total
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private final Map<UUID, Inventory> inventoryRepository = new HashMap<>();

    @Override
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        Inventory inventory = new Inventory(
                UUID.randomUUID(),
                request.carId(),
                request.availableUnits(),
                0,
                request.location(),
                Instant.now());

        inventoryRepository.put(inventory.inventoryId(), inventory);
        return mapToResponse(inventory);
    }

    @Override
    public Optional<InventoryResponse> getInventoryById(UUID inventoryId) {
        return Optional.ofNullable(inventoryRepository.get(inventoryId))
                .map(this::mapToResponse);
    }

    @Override
    public Optional<InventoryResponse> getInventoryByCarId(UUID carId) {
        return inventoryRepository.values()
                .stream()
                .filter(inv -> inv.carId().equals(carId))
                .findFirst()
                .map(this::mapToResponse);
    }

    @Override
    public List<InventoryResponse> listAllInventory() {
        return inventoryRepository.values()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryResponse updateInventory(UUID inventoryId, UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepository.get(inventoryId);
        if (inventory == null) {
            throw new RuntimeException("Inventory not found: " + inventoryId);
        }

        Inventory updatedInventory = new Inventory(
                inventory.inventoryId(),
                inventory.carId(),
                request.units(),
                inventory.reservedUnits(),
                request.location(),
                Instant.now());

        inventoryRepository.put(inventoryId, updatedInventory);
        return mapToResponse(updatedInventory);
    }

    @Override
    public InventoryResponse reserveInventory(UUID inventoryId, ReserveInventoryRequest request) {
        Inventory inventory = inventoryRepository.get(inventoryId);
        if (inventory == null) {
            throw new RuntimeException("Inventory not found: " + inventoryId);
        }

        if (inventory.availableUnits() < request.units()) {
            throw new RuntimeException("Insufficient available units. Available: " + inventory.availableUnits() +
                    ", Requested: " + request.units());
        }

        Inventory reservedInventory = new Inventory(
                inventory.inventoryId(),
                inventory.carId(),
                inventory.availableUnits() - request.units(),
                inventory.reservedUnits() + request.units(),
                inventory.location(),
                Instant.now());

        inventoryRepository.put(inventoryId, reservedInventory);
        return mapToResponse(reservedInventory);
    }

    @Override
    public InventoryResponse releaseInventory(UUID inventoryId, ReleaseInventoryRequest request) {
        Inventory inventory = inventoryRepository.get(inventoryId);
        if (inventory == null) {
            throw new RuntimeException("Inventory not found: " + inventoryId);
        }

        if (inventory.reservedUnits() < request.units()) {
            throw new RuntimeException(
                    "Cannot release more units than reserved. Reserved: " + inventory.reservedUnits() +
                            ", Requested: " + request.units());
        }

        Inventory releasedInventory = new Inventory(
                inventory.inventoryId(),
                inventory.carId(),
                inventory.availableUnits() + request.units(),
                inventory.reservedUnits() - request.units(),
                inventory.location(),
                Instant.now());

        inventoryRepository.put(inventoryId, releasedInventory);
        return mapToResponse(releasedInventory);
    }

    @Override
    public boolean isAvailable(UUID carId, int requiredUnits) {
        return inventoryRepository.values()
                .stream()
                .filter(inv -> inv.carId().equals(carId))
                .anyMatch(inv -> inv.availableUnits() >= requiredUnits);
    }

    /**
     * Convert Inventory model to InventoryResponse DTO
     */
    private InventoryResponse mapToResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.inventoryId(),
                inventory.carId(),
                inventory.availableUnits(),
                inventory.reservedUnits(),
                inventory.location(),
                inventory.lastUpdated());
    }
}
