package com.carplatform.catalog.service;

import com.carplatform.catalog.dto.CarResponse;
import com.carplatform.catalog.dto.CreateCarRequest;
import com.carplatform.catalog.dto.SearchCarRequest;
import com.carplatform.catalog.dto.UpdateCarRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Car Catalog Service Interface
 * 
 * Defines the contract for car management operations.
 * Implementation will handle in-memory operations initially.
 */
public interface CatalogService {

    /**
     * Create a new car
     */
    CarResponse createCar(CreateCarRequest request);

    /**
     * Get car by ID
     */
    Optional<CarResponse> getCarById(UUID carId);

    /**
     * List all cars
     */
    List<CarResponse> listAllCars();

    /**
     * Search cars with filters
     */
    List<CarResponse> searchCars(SearchCarRequest request);

    /**
     * Update car
     */
    CarResponse updateCar(UUID carId, UpdateCarRequest request);

    /**
     * Delete car (mark as discontinued)
     */
    void deleteCar(UUID carId);
}
