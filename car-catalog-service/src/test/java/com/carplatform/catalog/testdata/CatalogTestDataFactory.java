package com.carplatform.catalog.testdata;

import com.carplatform.catalog.dto.CreateCarRequest;
import com.carplatform.catalog.dto.SearchCarRequest;
import com.carplatform.catalog.dto.UpdateCarRequest;
import com.carplatform.catalog.model.Car;
import com.carplatform.catalog.model.CarStatus;
import com.carplatform.catalog.model.FuelType;
import com.carplatform.catalog.model.TransmissionType;

import java.math.BigDecimal;

public final class CatalogTestDataFactory {

    private CatalogTestDataFactory() {
    }

    public static CreateCarRequest validCreateRequest() {
        return new CreateCarRequest(
                "Toyota",
                "Corolla",
                "XLE",
                2024,
                FuelType.PETROL,
                TransmissionType.AUTOMATIC,
                BigDecimal.valueOf(25000),
                "Reliable sedan");
    }

    public static UpdateCarRequest validUpdateRequest() {
        return new UpdateCarRequest(
                "Toyota",
                "Corolla",
                "XSE",
                2025,
                BigDecimal.valueOf(27000),
                "Updated trim",
                CarStatus.ACTIVE);
    }

    public static SearchCarRequest brandSearchRequest() {
        return new SearchCarRequest("Toyota", null, null, null, null, null, null);
    }

    public static Car activeCar() {
        return new Car(
                "Toyota",
                "Corolla",
                "XLE",
                2024,
                FuelType.PETROL,
                TransmissionType.AUTOMATIC,
                BigDecimal.valueOf(25000),
                "Reliable sedan");
    }

    public static Car discontinuedCar() {
        Car car = activeCar();
        car.setStatus(CarStatus.DISCONTINUED);
        return car;
    }
}
