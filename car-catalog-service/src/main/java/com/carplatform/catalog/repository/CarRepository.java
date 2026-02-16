package com.carplatform.catalog.repository;

import com.carplatform.catalog.model.Car;
import com.carplatform.catalog.model.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Car entity
 * 
 * Provides CRUD operations and custom query methods.
 * Automatically generates SQL queries based on repository methods.
 */
@Repository
public interface CarRepository extends JpaRepository<Car, UUID> {

    /**
     * Find all active cars
     */
    List<Car> findByStatus(CarStatus status);

    /**
     * Find cars by brand
     */
    List<Car> findByBrandIgnoreCase(String brand);

    /**
     * Find cars by brand and model
     */
    List<Car> findByBrandIgnoreCaseAndModelIgnoreCase(String brand, String model);

    /**
     * Find cars within a price range
     */
    List<Car> findByPriceBetween(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);
}
