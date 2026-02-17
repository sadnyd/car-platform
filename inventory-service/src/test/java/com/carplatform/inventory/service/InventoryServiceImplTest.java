package com.carplatform.inventory.service;

import com.carplatform.inventory.dto.CreateInventoryRequest;
import com.carplatform.inventory.dto.InventoryResponse;
import com.carplatform.inventory.dto.ReleaseInventoryRequest;
import com.carplatform.inventory.dto.ReserveInventoryRequest;
import com.carplatform.inventory.model.Inventory;
import com.carplatform.inventory.repository.InventoryRepository;
import com.carplatform.inventory.testdata.InventoryTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryServiceImpl Business Rule Tests")
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void createInventoryShouldPersistEntity() {
        CreateInventoryRequest request = InventoryTestDataFactory.validCreateRequest();
        Inventory entity = InventoryTestDataFactory.inventoryEntity(10, 0);

        when(inventoryRepository.save(any(Inventory.class))).thenReturn(entity);

        InventoryResponse response = inventoryService.createInventory(request);

        assertEquals(10, response.availableUnits());
        assertEquals(0, response.reservedUnits());
    }

    @Test
    void reserveInventoryShouldDecreaseAvailableAndIncreaseReserved() {
        Inventory inventory = InventoryTestDataFactory.inventoryEntity(10, 2);
        ReserveInventoryRequest request = new ReserveInventoryRequest(inventory.getCarId(), 3);

        when(inventoryRepository.findById(inventory.getInventoryId())).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.reserveInventory(inventory.getInventoryId(), request);

        assertEquals(7, response.availableUnits());
        assertEquals(5, response.reservedUnits());
    }

    @Test
    void reserveInventoryShouldFailOnInsufficientUnits() {
        Inventory inventory = InventoryTestDataFactory.inventoryEntity(1, 0);
        ReserveInventoryRequest request = new ReserveInventoryRequest(inventory.getCarId(), 2);

        when(inventoryRepository.findById(inventory.getInventoryId())).thenReturn(Optional.of(inventory));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.reserveInventory(inventory.getInventoryId(), request));

        assertTrue(exception.getMessage().contains("Insufficient available units"));
    }

    @Test
    void releaseInventoryShouldMoveUnitsBackToAvailable() {
        Inventory inventory = InventoryTestDataFactory.inventoryEntity(4, 6);
        ReleaseInventoryRequest request = new ReleaseInventoryRequest(inventory.getCarId(), 2);

        when(inventoryRepository.findById(inventory.getInventoryId())).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.releaseInventory(inventory.getInventoryId(), request);

        assertEquals(6, response.availableUnits());
        assertEquals(4, response.reservedUnits());
    }

    @Test
    void isAvailableShouldReturnTrueWhenAnyInventoryMatches() {
        Inventory i1 = InventoryTestDataFactory.inventoryEntity(1, 0);
        Inventory i2 = InventoryTestDataFactory.inventoryEntity(5, 0);
        UUID carId = i1.getCarId();
        i2.setCarId(carId);

        when(inventoryRepository.findByCarId(carId)).thenReturn(List.of(i1, i2));

        assertTrue(inventoryService.isAvailable(carId, 3));
    }
}
