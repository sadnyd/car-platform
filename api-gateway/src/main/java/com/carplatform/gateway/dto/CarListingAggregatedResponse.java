package com.carplatform.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregated Response: Car Listing with Availability Flags
 * 
 * Combines data from:
 * - Catalog Service (all cars: make, model, year, price, color)
 * - Inventory Service (availability per car: status, available units)
 * 
 * Used for client-friendly car listing APIs.
 * Includes pagination metadata.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CarListingAggregatedResponse implements Serializable {

    private List<CarListItem> cars;
    private PaginationInfo pagination;
    private AggregationMetadata metadata;

    // ===================== Constructors =====================

    public CarListingAggregatedResponse() {
        this.cars = new ArrayList<>();
        this.pagination = new PaginationInfo();
        this.metadata = new AggregationMetadata();
    }

    public CarListingAggregatedResponse(List<CarListItem> cars, int totalCount, int pageSize, int currentPage) {
        this.cars = cars;
        this.pagination = new PaginationInfo(totalCount, pageSize, currentPage);
        this.metadata = new AggregationMetadata();
    }

    // ===================== Getters & Setters =====================

    public List<CarListItem> getCars() {
        return cars;
    }

    public void setCars(List<CarListItem> cars) {
        this.cars = cars;
    }

    public PaginationInfo getPagination() {
        return pagination;
    }

    public void setPagination(PaginationInfo pagination) {
        this.pagination = pagination;
    }

    public AggregationMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AggregationMetadata metadata) {
        this.metadata = metadata;
    }

    // ===================== Nested: CarListItem =====================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CarListItem {

        private UUID carId;
        private String make;
        private String model;
        private Integer year;
        private BigDecimal price;
        private String color;

        // Availability info (compact for list view)
        private String availabilityStatus; // IN_STOCK | OUT_OF_STOCK | UNKNOWN
        private Integer availableUnits;

        // ===== Constructors =====
        public CarListItem() {
        }

        public CarListItem(
                UUID carId,
                String make,
                String model,
                Integer year,
                double price,
                String availabilityStatus,
                Integer availableUnits) {
            this.carId = carId;
            this.make = make;
            this.model = model;
            this.year = year;
            this.price = BigDecimal.valueOf(price);
            this.color = null;
            this.availabilityStatus = availabilityStatus;
            this.availableUnits = availableUnits;
        }

        public CarListItem(
                UUID carId,
                String make,
                String model,
                Integer year,
                BigDecimal price,
                String color,
                String availabilityStatus,
                Integer availableUnits) {
            this.carId = carId;
            this.make = make;
            this.model = model;
            this.year = year;
            this.price = price;
            this.color = color;
            this.availabilityStatus = availabilityStatus;
            this.availableUnits = availableUnits;
        }

        // ===== Getters & Setters =====

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

        public String getAvailabilityStatus() {
            return availabilityStatus;
        }

        public void setAvailabilityStatus(String availabilityStatus) {
            this.availabilityStatus = availabilityStatus;
        }

        public Integer getAvailableUnits() {
            return availableUnits;
        }

        public void setAvailableUnits(Integer availableUnits) {
            this.availableUnits = availableUnits;
        }

        @Override
        public String toString() {
            return "CarListItem{" +
                    "carId=" + carId +
                    ", make='" + make + '\'' +
                    ", model='" + model + '\'' +
                    ", year=" + year +
                    ", price=" + price +
                    ", color='" + color + '\'' +
                    ", availabilityStatus='" + availabilityStatus + '\'' +
                    ", availableUnits=" + availableUnits +
                    '}';
        }
    }

    // ===================== Nested: PaginationInfo =====================

    public static class PaginationInfo {

        private Integer totalCount;
        private Integer pageSize;
        private Integer currentPage;

        // ===== Constructors =====
        public PaginationInfo() {
            this.totalCount = 0;
            this.pageSize = 20;
            this.currentPage = 1;
        }

        public PaginationInfo(int totalCount, int pageSize, int currentPage) {
            this.totalCount = totalCount;
            this.pageSize = pageSize;
            this.currentPage = currentPage;
        }

        // ===== Getters & Setters =====

        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }

        public Integer getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(Integer currentPage) {
            this.currentPage = currentPage;
        }

        public Integer getTotalPages() {
            if (totalCount == null || totalCount == 0) {
                return 0;
            }
            return (int) Math.ceil((double) totalCount / pageSize);
        }

        @Override
        public String toString() {
            return "PaginationInfo{" +
                    "totalCount=" + totalCount +
                    ", pageSize=" + pageSize +
                    ", currentPage=" + currentPage +
                    '}';
        }
    }

    // ===================== Nested: AggregationMetadata =====================

    public static class AggregationMetadata {

        private LocalDateTime aggregatedAt;
        private String[] sources; // ["catalog:v1", "inventory:v1"]

        public AggregationMetadata() {
            this.aggregatedAt = LocalDateTime.now();
            this.sources = new String[] { "catalog:v1", "inventory:v1" };
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
    }

    // ===================== ToString =====================

    @Override
    public String toString() {
        return "CarListingAggregatedResponse{" +
                "carCount=" + (cars != null ? cars.size() : 0) +
                ", pagination=" + pagination +
                ", metadata=" + metadata +
                '}';
    }
}
