package com.carplatform.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from Catalog Service - Car Details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarDetailsResponse {

    private String carId;
    private String brand;
    private String model;
    private int year;
    private double price;
    private String status;
    private String description;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("message")
    private String message;
}
