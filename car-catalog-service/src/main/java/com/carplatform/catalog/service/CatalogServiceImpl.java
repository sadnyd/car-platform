package com.carplatform.catalog.service;

import com.carplatform.catalog.dto.CarResponse;
import com.carplatform.catalog.dto.CreateCarRequest;
import com.carplatform.catalog.dto.SearchCarRequest;
import com.carplatform.catalog.dto.UpdateCarRequest;
import com.carplatform.catalog.model.Car;
import com.carplatform.catalog.model.CarStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-Memory Implementation of Car Catalog Service
 * 
 * Uses a HashMap to store cars for Phase 3 (no database).
 * This will be replaced with database backend in Phase 4.
 */
@Service
public class CatalogServiceImpl implements CatalogService {

    private final Map<UUID, Car> carRepository = new HashMap<>();

    @Override
    public CarResponse createCar(CreateCarRequest request) {
        Car car = new Car(
                UUID.randomUUID(),
                request.brand(),
                request.model(),
                request.variant(),
                request.manufacturingYear(),
                request.fuelType(),
                request.transmissionType(),
                request.price(),
                CarStatus.ACTIVE,
                request.description(),
                Instant.now());

        carRepository.put(car.carId(), car);
        return mapToResponse(car);
    }

    @Override
    public Optional<CarResponse> getCarById(UUID carId) {
        return Optional.ofNullable(carRepository.get(carId))
                .map(this::mapToResponse);
    }

    @Override
    public List<CarResponse> listAllCars() {
        return carRepository.values()
                .stream()
                .filter(car -> car.status() == CarStatus.ACTIVE)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarResponse> searchCars(SearchCarRequest request) {
        return carRepository.values()
                .stream()
                .filter(car -> car.status() == CarStatus.ACTIVE)
                .filter(car -> request.brand() == null || car.brand().equalsIgnoreCase(request.brand()))
                .filter(car -> request.model() == null || car.model().equalsIgnoreCase(request.model()))
                .filter(car -> request.fuelType() == null || car.fuelType() == request.fuelType())
                .filter(car -> request.transmissionType() == null
                        || car.transmissionType() == request.transmissionType())
                .filter(car -> request.minPrice() == null || car.price().compareTo(request.minPrice()) >= 0)
                .filter(car -> request.maxPrice() == null || car.price().compareTo(request.maxPrice()) <= 0)
                .filter(car -> request.minYear() == null || car.manufacturingYear() >= request.minYear())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CarResponse updateCar(UUID carId, UpdateCarRequest request) {
        Car car = carRepository.get(carId);
        if (car == null) {
            throw new RuntimeException("Car not found: " + carId);
        }

        Car updatedCar = new Car(
                car.carId(),
                request.brand() != null ? request.brand() : car.brand(),
                request.model() != null ? request.model() : car.model(),
                request.variant() != null ? request.variant() : car.variant(),
                request.manufacturingYear() != null ? request.manufacturingYear() : car.manufacturingYear(),
                car.fuelType(),
                car.transmissionType(),
                request.price() != null ? request.price() : car.price(),
                request.status() != null ? request.status() : car.status(),
                request.description() != null ? request.description() : car.description(),
                car.createdAt());

        carRepository.put(carId, updatedCar);
        return mapToResponse(updatedCar);
    }

    @Override
    public void deleteCar(UUID carId) {
        Car car = carRepository.get(carId);
        if (car != null) {
            Car discontinued = new Car(
                    car.carId(),
                    car.brand(),
                    car.model(),
                    car.variant(),
                    car.manufacturingYear(),
                    car.fuelType(),
                    car.transmissionType(),
                    car.price(),
                    CarStatus.DISCONTINUED,
                    car.description(),
                    car.createdAt());
            carRepository.put(carId, discontinued);
        }
    }

    /**
     * Convert Car model to CarResponse DTO
     */
    private CarResponse mapToResponse(Car car) {
        return new CarResponse(
                car.carId(),
                car.brand(),
                car.model(),
                car.variant(),
                car.manufacturingYear(),
                car.fuelType(),
                car.transmissionType(),
                car.price(),
                car.status(),
                car.description(),
                car.createdAt());
    }
}
