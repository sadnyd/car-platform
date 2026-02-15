package com.carplatform.inventory.controller;

import com.carplatform.inventory.dto.CreateInventoryRequest;
import com.carplatform.inventory.dto.ReleaseInventoryRequest;
import com.carplatform.inventory.dto.ReserveInventoryRequest;
import com.carplatform.inventory.dto.UpdateInventoryRequest;
import com.carplatform.inventory.dto.InventoryResponse;
import com.carplatform.inventory.service.InventoryService;
import com.carplatform.inventory.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

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
}
