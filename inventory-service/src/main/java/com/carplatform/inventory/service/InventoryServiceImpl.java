package com.carplatform.inventory.service;

import com.carplatform.inventory.dto.InventoryResponse;
import com.carplatform.inventory.dto.CreateInventoryRequest;
import com.carplatform.inventory.dto.UpdateInventoryRequest;
import com.carplatform.inventory.dto.ReserveInventoryRequest;
import com.carplatform.inventory.dto.ReleaseInventoryRequest;
import com.carplatform.inventory.model.Inventory;
import com.carplatform.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Database-backed Implementation of Inventory Service
 * 
 * Uses InventoryRepository (Spring Data JPA) to persist inventory in
 * PostgreSQL.
 * Business Logic: availableUnits + reservedUnits <= total
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        Inventory inventory = new Inventory();
        // Don't set inventoryId - let Hibernate/JPA generate it with @GeneratedValue
        inventory.setCarId(request.carId());
        inventory.setAvailableUnits(request.availableUnits());
        inventory.setReservedUnits(0);
        inventory.setLocation(request.location());
        inventory.setLastUpdated(Instant.now());

        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToResponse(savedInventory);
    }

    @Override
    public Optional<InventoryResponse> getInventoryById(UUID inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .map(this::mapToResponse);
    }

    @Override
    public Optional<InventoryResponse> getInventoryByCarId(UUID carId) {
        return inventoryRepository.findByCarId(carId)
                .stream()
                .findFirst()
                .map(this::mapToResponse);
    }

    @Override
    public List<InventoryResponse> listAllInventory() {
        return inventoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryResponse updateInventory(UUID inventoryId, UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found: " + inventoryId));

        inventory.setAvailableUnits(request.units());
        inventory.setLocation(request.location());
        inventory.setLastUpdated(Instant.now());

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return mapToResponse(updatedInventory);
    }

    @Override
    public InventoryResponse reserveInventory(UUID inventoryId, ReserveInventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found: " + inventoryId));

        if (inventory.getAvailableUnits() < request.units()) {
            throw new RuntimeException("Insufficient available units. Available: " + inventory.getAvailableUnits() +
                    ", Requested: " + request.units());
        }

        inventory.setAvailableUnits(inventory.getAvailableUnits() - request.units());
        inventory.setReservedUnits(inventory.getReservedUnits() + request.units());
        inventory.setLastUpdated(Instant.now());

        Inventory reservedInventory = inventoryRepository.save(inventory);
        return mapToResponse(reservedInventory);
    }

    @Override
    public InventoryResponse releaseInventory(UUID inventoryId, ReleaseInventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found: " + inventoryId));

        if (inventory.getReservedUnits() < request.units()) {
            throw new RuntimeException(
                    "Cannot release more units than reserved. Reserved: " + inventory.getReservedUnits() +
                            ", Requested: " + request.units());
        }

        inventory.setAvailableUnits(inventory.getAvailableUnits() + request.units());
        inventory.setReservedUnits(inventory.getReservedUnits() - request.units());
        inventory.setLastUpdated(Instant.now());

        Inventory releasedInventory = inventoryRepository.save(inventory);
        return mapToResponse(releasedInventory);
    }

    @Override
    public boolean isAvailable(UUID carId, int requiredUnits) {
        return inventoryRepository.findByCarId(carId)
                .stream()
                .anyMatch(inv -> inv.getAvailableUnits() >= requiredUnits);
    }

    /**
     * Convert Inventory model to InventoryResponse DTO
     */
    private InventoryResponse mapToResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getInventoryId(),
                inventory.getCarId(),
                inventory.getAvailableUnits(),
                inventory.getReservedUnits(),
                inventory.getLocation(),
                inventory.getLastUpdated());
    }
}
