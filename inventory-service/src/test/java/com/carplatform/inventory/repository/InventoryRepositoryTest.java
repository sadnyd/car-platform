package com.carplatform.inventory.repository;

import com.carplatform.inventory.model.Inventory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("InventoryRepository JPA Tests")
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void shouldSaveAndFindByCarId() {
        UUID carId = UUID.randomUUID();
        inventoryRepository.save(buildEntity(carId, "warehouse-a", 10, 0));

        List<Inventory> result = inventoryRepository.findByCarId(carId);

        assertEquals(1, result.size());
        assertEquals(carId, result.get(0).getCarId());
    }

    @Test
    void shouldFindByLocation() {
        inventoryRepository.save(buildEntity(UUID.randomUUID(), "warehouse-b", 4, 1));
        inventoryRepository.save(buildEntity(UUID.randomUUID(), "warehouse-b", 6, 0));

        List<Inventory> result = inventoryRepository.findByLocation("warehouse-b");

        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmptyWhenNoRowsMatch() {
        List<Inventory> result = inventoryRepository.findByLocation("missing-location");

        assertTrue(result.isEmpty());
    }

    private Inventory buildEntity(UUID carId, String location, int available, int reserved) {
        Inventory inventory = new Inventory();
        inventory.setCarId(carId);
        inventory.setLocation(location);
        inventory.setAvailableUnits(available);
        inventory.setReservedUnits(reserved);
        inventory.setLastUpdated(Instant.now());
        return inventory;
    }
}
