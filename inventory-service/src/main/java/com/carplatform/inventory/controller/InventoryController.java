package com.carplatform.inventory.controller;

import com.carplatform.inventory.dto.CreateInventoryRequest;
import com.carplatform.inventory.dto.ReleaseInventoryRequest;
import com.carplatform.inventory.dto.ReserveInventoryRequest;
import com.carplatform.inventory.dto.UpdateInventoryRequest;
import com.carplatform.inventory.dto.InventoryResponse;
import com.carplatform.inventory.dto.AvailabilityCheckResponse;
import com.carplatform.inventory.dto.ReservationRequest;
import com.carplatform.inventory.dto.ReservationResponse;
import com.carplatform.inventory.service.InventoryService;
import com.carplatform.inventory.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // ===== ORIGINAL CRUD ENDPOINTS =====

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
            @RequestBody @Valid CreateInventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(request));
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable UUID inventoryId) {
        return inventoryService.getInventoryById(inventoryId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> listAll() {
        return ResponseEntity.ok(inventoryService.listAllInventory());
    }

    @PutMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable UUID inventoryId,
            @RequestBody @Valid UpdateInventoryRequest request) {
        return ResponseEntity.ok(inventoryService.updateInventory(inventoryId, request));
    }

    @PostMapping("/{inventoryId}/reserve")
    public ResponseEntity<InventoryResponse> reserve(
            @PathVariable UUID inventoryId,
            @RequestBody @Valid ReserveInventoryRequest request) {
        return ResponseEntity.ok(inventoryService.reserveInventory(inventoryId, request));
    }

    @PostMapping("/{inventoryId}/release")
    public ResponseEntity<InventoryResponse> release(
            @PathVariable UUID inventoryId,
            @RequestBody @Valid ReleaseInventoryRequest request) {
        return ResponseEntity.ok(inventoryService.releaseInventory(inventoryId, request));
    }

    // ===== PHASE 5 INTER-SERVICE APIs =====

    /**
     * PHASE 5.3: Check availability for a car
     * 
     * Called by: Order Service (when creating an order)
     * Purpose: Validate that stock is available before order creation
     * 
     * @param carId UUID of car to check
     * @return 200 with availability details, or 404 if car not in inventory
     */
    @GetMapping("/check-availability/{carId}")
    public ResponseEntity<AvailabilityCheckResponse> checkAvailability(@PathVariable String carId) {
        log.debug("Availability check requested for car: {}", carId);

        UUID carUUID;
        try {
            carUUID = UUID.fromString(carId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid car ID format: {}", carId);
            return ResponseEntity.badRequest()
                    .body(AvailabilityCheckResponse.notFound(carId));
        }

        var inventoryOpt = inventoryService.getInventoryByCarId(carUUID);

        if (inventoryOpt.isEmpty()) {
            log.warn("Car not found in inventory: {}", carId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AvailabilityCheckResponse.notFound(carId));
        }

        InventoryResponse inventory = inventoryOpt.get();
        int totalUnits = inventory.availableUnits() + inventory.reservedUnits();

        if (inventory.availableUnits() <= 0) {
            log.warn("Car out of stock: {} (total units: {})", carId, totalUnits);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AvailabilityCheckResponse.outOfStock(carId, totalUnits));
        }

        log.info("Car available: {} (available: {}, reserved: {})",
                carId, inventory.availableUnits(), inventory.reservedUnits());

        return ResponseEntity.ok(AvailabilityCheckResponse.success(
                carId,
                totalUnits,
                inventory.reservedUnits(),
                inventory.availableUnits()));
    }

    /**
     * PHASE 5.4: Reserve inventory for an order
     * 
     * Called by: Order Service (after availability check passes)
     * Purpose: Lock stock units for the order
     * 
     * Business logic:
     * - Check available units
     * - Decrement availableUnits
     * - Increment reservedUnits
     * - Set reservation expiry
     * 
     * @param request Includes carId, orderId, units to reserve
     * @return 201 with reservation details, or error code if cannot reserve
     */
    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserveInventory(
            @RequestBody @Valid ReservationRequest request) {

        log.debug("Reservation request - car: {}, order: {}, units: {}",
                request.getCarId(), request.getOrderId(), request.getUnits());

        UUID carUUID;
        try {
            carUUID = UUID.fromString(request.getCarId());
        } catch (IllegalArgumentException e) {
            log.error("Invalid car ID format in reservation: {}", request.getCarId());
            return ResponseEntity.badRequest()
                    .body(ReservationResponse.carNotFound(request.getCarId()));
        }

        var inventoryOpt = inventoryService.getInventoryByCarId(carUUID);

        if (inventoryOpt.isEmpty()) {
            log.warn("Car not found for reservation: {}", request.getCarId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ReservationResponse.carNotFound(request.getCarId()));
        }

        InventoryResponse inventory = inventoryOpt.get();

        // Check if we have enough available units
        if (inventory.availableUnits() < request.getUnits()) {
            log.warn("Insufficient stock for reservation - car: {}, requested: {}, available: {}",
                    request.getCarId(), request.getUnits(), inventory.availableUnits());

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ReservationResponse.insufficientStock(
                            request.getCarId(),
                            request.getOrderId()));
        }

        // Perform the reservation by reducing available and increasing reserved
        ReserveInventoryRequest reserveRequest = new ReserveInventoryRequest(carUUID, request.getUnits());
        InventoryResponse updatedInventory = inventoryService.reserveInventory(
                inventory.inventoryId(),
                reserveRequest);

        log.info("Reservation successful - order: {}, units: {}, remaining: {}",
                request.getOrderId(), request.getUnits(), updatedInventory.availableUnits());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.success(
                        updatedInventory.inventoryId(),
                        carUUID,
                        request.getOrderId(),
                        request.getUnits(),
                        updatedInventory.availableUnits()));
    }
}
