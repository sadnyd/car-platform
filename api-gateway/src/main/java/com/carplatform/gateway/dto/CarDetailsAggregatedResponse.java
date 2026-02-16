package com.carplatform.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * Aggregated Response: Car Details with Availability
 * 
 * Combines data from:
 * - Catalog Service (car details: make, model, year, price, color)
 * - Inventory Service (availability: status, stock counts)
 * 
 * This is a GATEWAY-level DTO, not service-specific.
 * Used for client-friendly APIs that aggregate multiple services.
 * 
 * Phase 6: Aggregation Pattern
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CarDetailsAggregatedResponse implements Serializable {

    private UUID carId;
    private String make;
    private String model;
    private Integer year;
    private BigDecimal price;
    private String color;

    private AvailabilityInfo availability;
    private AggregationMetadata metadata;

    // ===================== Constructors =====================

    public CarDetailsAggregatedResponse() {
    }

    public CarDetailsAggregatedResponse(
            UUID carId,
            String make,
            String model,
            Integer year,
            double price,
            String color,
            AvailabilityInfo availability) {
        this(carId, make, model, year, BigDecimal.valueOf(price), color, availability, new AggregationMetadata());
    }

    public CarDetailsAggregatedResponse(
            UUID carId,
            String make,
            String model,
            Integer year,
            double price,
            String color,
            AvailabilityInfo availability,
            AggregationMetadata metadata) {
        this(carId, make, model, year, BigDecimal.valueOf(price), color, availability, metadata);
    }

    @Builder
    public CarDetailsAggregatedResponse(
            UUID carId,
            String make,
            String model,
            Integer year,
            BigDecimal price,
            String color,
            AvailabilityInfo availability,
            AggregationMetadata metadata) {
        this.carId = carId;
        this.make = make;
        this.model = model;
        this.year = year;
        this.price = price;
        this.color = color;
        this.availability = availability;
        this.metadata = metadata != null ? metadata : new AggregationMetadata();
    }

    // ===================== Getters & Setters =====================

    public UUID getCarId() {
        return carId;
    }

    public void setCarId(UUID carId) {
        this.carId = carId;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public AvailabilityInfo getAvailability() {
        return availability;
    }

    public void setAvailability(AvailabilityInfo availability) {
        this.availability = availability;
    }

    public AggregationMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AggregationMetadata metadata) {
        this.metadata = metadata;
    }

    // ===================== Nested: AvailabilityInfo =====================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AvailabilityInfo {

        public enum Status {
            IN_STOCK,
            OUT_OF_STOCK,
            UNKNOWN
        }

        private Status status;
        private Integer totalUnits;
        private Integer availableUnits;
        private Integer reservedUnits;
        private String reason; // Fallback reason if unavailable

        // ===== Constructors =====
        public AvailabilityInfo() {
        }

        public AvailabilityInfo(Status status) {
            this.status = status;
        }

        public AvailabilityInfo(Status status, Integer totalUnits, Integer availableUnits, Integer reservedUnits) {
            this.status = status;
            this.totalUnits = totalUnits;
            this.availableUnits = availableUnits;
            this.reservedUnits = reservedUnits;
        }

        // ===== Factory Methods =====

        public static AvailabilityInfo inStock(String carId, int totalUnits, int availableUnits, int reservedUnits) {
            return new AvailabilityInfo(Status.IN_STOCK, totalUnits, availableUnits, reservedUnits);
        }

        public static AvailabilityInfo inStock(int totalUnits, int availableUnits, int reservedUnits) {
            return new AvailabilityInfo(Status.IN_STOCK, totalUnits, availableUnits, reservedUnits);
        }

        public static AvailabilityInfo outOfStock() {
            AvailabilityInfo info = new AvailabilityInfo(Status.OUT_OF_STOCK, 0, 0, 0);
            info.reason = "Car is not in stock";
            return info;
        }

        public static AvailabilityInfo unknown(String reason) {
            AvailabilityInfo info = new AvailabilityInfo(Status.UNKNOWN);
            info.reason = reason;
            return info;
        }

        // ===== Getters & Setters =====

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public Integer getTotalUnits() {
            return totalUnits;
        }

        public void setTotalUnits(Integer totalUnits) {
            this.totalUnits = totalUnits;
        }

        public Integer getAvailableUnits() {
            return availableUnits;
        }

        public void setAvailableUnits(Integer availableUnits) {
            this.availableUnits = availableUnits;
        }

        public Integer getReservedUnits() {
            return reservedUnits;
        }

        public void setReservedUnits(Integer reservedUnits) {
            this.reservedUnits = reservedUnits;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    // ===================== Nested: AggregationMetadata =====================

    public static class AggregationMetadata {

        private LocalDateTime aggregatedAt;
        private String[] sources; // ["catalog:v1", "inventory:v1"]
        private int aggregationStatus;

        public AggregationMetadata() {
            this.aggregatedAt = LocalDateTime.now();
            this.sources = new String[] { "catalog:v1", "inventory:v1" };
            this.aggregationStatus = 200;
        }

        public LocalDateTime getAggregatedAt() {
            return aggregatedAt;
        }

        public void setAggregatedAt(LocalDateTime aggregatedAt) {
            this.aggregatedAt = aggregatedAt;
        }

        public String[] getSources() {
            return sources;
        }

        public void setSources(String[] sources) {
            this.sources = sources;
        }

        public int getAggregationStatus() {
            return aggregationStatus;
        }

        public void setAggregationStatus(int aggregationStatus) {
            this.aggregationStatus = aggregationStatus;
        }
    }

    public static class CarDetailsAggregatedResponseBuilder {
        public CarDetailsAggregatedResponseBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public CarDetailsAggregatedResponseBuilder price(double price) {
            this.price = BigDecimal.valueOf(price);
            return this;
        }

        public CarDetailsAggregatedResponseBuilder metadata(AggregationMetadata metadata) {
            this.metadata = metadata != null ? metadata : new AggregationMetadata();
            return this;
        }
    }

    // ===================== ToString =====================

    @Override
    public String toString() {
        return "CarDetailsAggregatedResponse{" +
                "carId=" + carId +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", price=" + price +
                ", color='" + color + '\'' +
                ", availability=" + availability +
                ", metadata=" + metadata +
                '}';
    }
}
