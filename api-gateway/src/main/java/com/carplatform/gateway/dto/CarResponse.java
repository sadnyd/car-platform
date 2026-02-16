package com.carplatform.gateway.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Car Response DTO
 * 
 * Represents a car as returned from the Catalog Service.
 * This is a mapping DTO used internally in the Gateway.
 * 
 * Phase 6: Aggregation Pattern - Service Response Mapping
 */
public class CarResponse implements Serializable {

    private UUID id;
    private String make;
    private String model;
    private Integer year;
    private BigDecimal price;
    private String color;

    // ===================== Constructors =====================

    public CarResponse() {
    }

    public CarResponse(UUID id, String make, String model, Integer year, BigDecimal price, String color) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.year = year;
        this.price = price;
        this.color = color;
    }

    // ===================== Getters & Setters =====================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setPrice(double price) {
        this.price = BigDecimal.valueOf(price);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "CarResponse{" +
                "id=" + id +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", price=" + price +
                ", color='" + color + '\'' +
                '}';
    }
}
