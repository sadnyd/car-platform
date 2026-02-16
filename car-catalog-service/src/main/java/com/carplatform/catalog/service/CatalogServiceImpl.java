package com.carplatform.catalog.service;

import com.carplatform.catalog.dto.CarResponse;
import com.carplatform.catalog.dto.CreateCarRequest;
import com.carplatform.catalog.dto.SearchCarRequest;
import com.carplatform.catalog.dto.UpdateCarRequest;
import com.carplatform.catalog.model.Car;
import com.carplatform.catalog.model.CarStatus;
import com.carplatform.catalog.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Car Catalog Service - Database-backed implementation
 * 
 * Uses Spring Data JPA repository to persist and retrieve cars from PostgreSQL.
 */
@Service
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    private CarRepository carRepository;

    @Override
    public CarResponse createCar(CreateCarRequest request) {
        Car car = new Car(
                request.brand(),
                request.model(),
                request.variant(),
                request.manufacturingYear(),
                request.fuelType(),
                request.transmissionType(),
                request.price(),
                request.description());

        Car savedCar = carRepository.save(car);
        return mapToResponse(savedCar);
    }

    @Override
    public Optional<CarResponse> getCarById(UUID carId) {
        return carRepository.findById(carId)
                .map(this::mapToResponse);
    }

    @Override
    public List<CarResponse> listAllCars() {
        return carRepository.findByStatus(CarStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarResponse> searchCars(SearchCarRequest request) {
        List<Car> cars = carRepository.findByStatus(CarStatus.ACTIVE);
        
        return cars.stream()
                .filter(car -> request.brand() == null || car.getBrand().equalsIgnoreCase(request.brand()))
                .filter(car -> request.model() == null || car.getModel().equalsIgnoreCase(request.model()))
                .filter(car -> request.fuelType() == null || car.getFuelType() == request.fuelType())
                .filter(car -> request.transmissionType() == null
                        || car.getTransmissionType() == request.transmissionType())
                .filter(car -> request.minPrice() == null || car.getPrice().compareTo(request.minPrice()) >= 0)
                .filter(car -> request.maxPrice() == null || car.getPrice().compareTo(request.maxPrice()) <= 0)
                .filter(car -> request.minYear() == null || car.getManufacturingYear() >= request.minYear())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CarResponse updateCar(UUID carId, UpdateCarRequest request) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found: " + carId));

        if (request.brand() != null) car.setBrand(request.brand());
        if (request.model() != null) car.setModel(request.model());
        if (request.variant() != null) car.setVariant(request.variant());
        if (request.manufacturingYear() != null) car.setManufacturingYear(request.manufacturingYear());
        if (request.price() != null) car.setPrice(request.price());
        if (request.status() != null) car.setStatus(request.status());
        if (request.description() != null) car.setDescription(request.description());
        car.setLastUpdated(Instant.now());

        Car updatedCar = carRepository.save(car);
        return mapToResponse(updatedCar);
    }

    @Override
    public void deleteCar(UUID carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found: " + carId));
        
        car.setStatus(CarStatus.DISCONTINUED);
        car.setLastUpdated(Instant.now());
        carRepository.save(car);
    }

    /**
     * Convert Car entity to CarResponse DTO
     */
    private CarResponse mapToResponse(Car car) {
        return new CarResponse(
                car.getCarId(),
                car.getBrand(),
                car.getModel(),
                car.getVariant(),
                car.getManufacturingYear(),
                car.getFuelType(),
                car.getTransmissionType(),
                car.getPrice(),
                car.getStatus(),
                car.getDescription(),
                car.getCreatedAt());
    }
}
