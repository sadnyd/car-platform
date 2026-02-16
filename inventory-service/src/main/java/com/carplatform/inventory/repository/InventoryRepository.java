package com.carplatform.inventory.repository;

import com.carplatform.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Inventory entity persistence operations.
 * Extends JpaRepository to provide CRUD and query capabilities.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    /**
     * Find inventory records by carId.
     * 
     * @param carId the car identifier
     * @return list of inventory records for the car
     */
    List<Inventory> findByCarId(UUID carId);

    /**
     * Find inventory records by location.
     * 
     * @param location the storage/warehouse location
     * @return list of inventory records at the location
     */
    List<Inventory> findByLocation(String location);
}
