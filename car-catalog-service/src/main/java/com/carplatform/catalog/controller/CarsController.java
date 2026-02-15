package com.carplatform.catalog.controller;

import com.carplatform.catalog.dto.CreateCarRequest;
import com.carplatform.catalog.dto.SearchCarRequest;
import com.carplatform.catalog.dto.UpdateCarRequest;
import com.carplatform.catalog.dto.CarResponse;
import com.carplatform.catalog.service.CatalogService;
import com.carplatform.catalog.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cars")
public class CarsController {

    @Autowired
    private CatalogService catalogService;

    @PostMapping
    public ResponseEntity<CarResponse> createCar(@RequestBody @Valid CreateCarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogService.createCar(request));
    }

    @GetMapping("/{carId}")
    public ResponseEntity<CarResponse> getCarById(@PathVariable UUID carId) {
        return catalogService.getCarById(carId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found: " + carId));
    }

    @GetMapping
    public ResponseEntity<List<CarResponse>> listAllCars() {
        return ResponseEntity.ok(catalogService.listAllCars());
    }

    @PostMapping("/search")
    public ResponseEntity<List<CarResponse>> searchCars(@RequestBody SearchCarRequest request) {
        return ResponseEntity.ok(catalogService.searchCars(request));
    }

    @PutMapping("/{carId}")
    public ResponseEntity<CarResponse> updateCar(
            @PathVariable UUID carId,
            @RequestBody @Valid UpdateCarRequest request) {
        return ResponseEntity.ok(catalogService.updateCar(carId, request));
    }

    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deleteCar(@PathVariable UUID carId) {
        catalogService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }
}
