package com.carplatform.inventory.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory")
public class Inventory {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "inventory_id")
        private UUID inventoryId;

        @Column(name = "car_id", nullable = false)
        private UUID carId;

        @Column(name = "available_units", nullable = false)
        private int availableUnits;

        @Column(name = "reserved_units", nullable = false)
        private int reservedUnits;

        @Column(name = "location", length = 255)
        private String location;

        @Column(name = "last_updated", nullable = false)
        private Instant lastUpdated;

        // Constructors
        public Inventory() {
        }

        public Inventory(UUID inventoryId, UUID carId, int availableUnits, int reservedUnits, String location,
                        Instant lastUpdated) {
                this.inventoryId = inventoryId;
                this.carId = carId;
                this.availableUnits = availableUnits;
                this.reservedUnits = reservedUnits;
                this.location = location;
                this.lastUpdated = lastUpdated;
        }

        // Getters and Setters
        public UUID getInventoryId() {
                return inventoryId;
        }

        public void setInventoryId(UUID inventoryId) {
                this.inventoryId = inventoryId;
        }

        public UUID getCarId() {
                return carId;
        }

        public void setCarId(UUID carId) {
                this.carId = carId;
        }

        public int getAvailableUnits() {
                return availableUnits;
        }

        public void setAvailableUnits(int availableUnits) {
                this.availableUnits = availableUnits;
        }

        public int getReservedUnits() {
                return reservedUnits;
        }

        public void setReservedUnits(int reservedUnits) {
                this.reservedUnits = reservedUnits;
        }

        public String getLocation() {
                return location;
        }

        public void setLocation(String location) {
                this.location = location;
        }

        public Instant getLastUpdated() {
                return lastUpdated;
        }

        public void setLastUpdated(Instant lastUpdated) {
                this.lastUpdated = lastUpdated;
        }
}
