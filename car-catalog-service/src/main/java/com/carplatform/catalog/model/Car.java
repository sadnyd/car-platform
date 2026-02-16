package com.carplatform.catalog.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cars")
public class Car {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "car_id")
    private UUID carId;
    
    @Column(name = "brand", nullable = false, length = 50)
    private String brand;
    
    @Column(name = "model", nullable = false, length = 50)
    private String model;
    
    @Column(name = "variant", nullable = false, length = 50)
    private String variant;
    
    @Column(name = "manufacturing_year", nullable = false)
    private int manufacturingYear;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false)
    private FuelType fuelType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transmission_type", nullable = false)
    private TransmissionType transmissionType;
    
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CarStatus status;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;
    
    // Constructors
    public Car() {
    }
    
    public Car(String brand, String model, String variant, int manufacturingYear,
               FuelType fuelType, TransmissionType transmissionType, BigDecimal price, String description) {
        this.brand = brand;
        this.model = model;
        this.variant = variant;
        this.manufacturingYear = manufacturingYear;
        this.fuelType = fuelType;
        this.transmissionType = transmissionType;
        this.price = price;
        this.description = description;
        this.status = CarStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.lastUpdated = Instant.now();
    }
    
    // Getters and Setters
    public UUID getCarId() {
        return carId;
    }
    
    public void setCarId(UUID carId) {
        this.carId = carId;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getVariant() {
        return variant;
    }
    
    public void setVariant(String variant) {
        this.variant = variant;
    }
    
    public int getManufacturingYear() {
        return manufacturingYear;
    }
    
    public void setManufacturingYear(int manufacturingYear) {
        this.manufacturingYear = manufacturingYear;
    }
    
    public FuelType getFuelType() {
        return fuelType;
    }
    
    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }
    
    public TransmissionType getTransmissionType() {
        return transmissionType;
    }
    
    public void setTransmissionType(TransmissionType transmissionType) {
        this.transmissionType = transmissionType;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public CarStatus getStatus() {
        return status;
    }
    
    public void setStatus(CarStatus status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
