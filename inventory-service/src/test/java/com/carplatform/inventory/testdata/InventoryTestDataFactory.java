package com.carplatform.inventory.testdata;

import com.carplatform.inventory.dto.CreateInventoryRequest;
import com.carplatform.inventory.dto.ReserveInventoryRequest;
import com.carplatform.inventory.dto.UpdateInventoryRequest;
import com.carplatform.inventory.model.Inventory;

import java.time.Instant;
import java.util.UUID;

public final class InventoryTestDataFactory {

    public static final UUID CAR_ID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    public static final UUID INVENTORY_ID = UUID.fromString("3d594650-3436-4f44-8f58-310f5cc72f15");

    private InventoryTestDataFactory() {
    }

    public static CreateInventoryRequest validCreateRequest() {
        return new CreateInventoryRequest(CAR_ID, 10, "warehouse-a");
    }

    public static UpdateInventoryRequest validUpdateRequest() {
        return new UpdateInventoryRequest(CAR_ID, 8, "warehouse-b");
    }

    public static ReserveInventoryRequest reserveTwoUnits() {
        return new ReserveInventoryRequest(CAR_ID, 2);
    }

    public static Inventory inventoryEntity(int available, int reserved) {
        Inventory inventory = new Inventory();
        inventory.setInventoryId(INVENTORY_ID);
        inventory.setCarId(CAR_ID);
        inventory.setAvailableUnits(available);
        inventory.setReservedUnits(reserved);
        inventory.setLocation("warehouse-a");
        inventory.setLastUpdated(Instant.now());
        return inventory;
    }
}
