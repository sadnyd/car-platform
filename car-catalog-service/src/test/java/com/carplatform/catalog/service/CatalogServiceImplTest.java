package com.carplatform.catalog.service;

import com.carplatform.catalog.dto.CarResponse;
import com.carplatform.catalog.dto.CreateCarRequest;
import com.carplatform.catalog.dto.SearchCarRequest;
import com.carplatform.catalog.dto.UpdateCarRequest;
import com.carplatform.catalog.model.Car;
import com.carplatform.catalog.model.CarStatus;
import com.carplatform.catalog.repository.CarRepository;
import com.carplatform.catalog.testdata.CatalogTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogServiceImpl Business Rule Tests")
class CatalogServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    @Test
    void createCarShouldPersistAndReturnResponse() {
        CreateCarRequest request = CatalogTestDataFactory.validCreateRequest();
        Car savedCar = CatalogTestDataFactory.activeCar();

        when(carRepository.save(any(Car.class))).thenReturn(savedCar);

        CarResponse response = catalogService.createCar(request);

        assertEquals("Toyota", response.brand());
        assertEquals(CarStatus.ACTIVE, response.status());
    }

    @Test
    void listAllCarsShouldReturnOnlyActiveCars() {
        when(carRepository.findByStatus(CarStatus.ACTIVE)).thenReturn(List.of(CatalogTestDataFactory.activeCar()));

        List<CarResponse> cars = catalogService.listAllCars();

        assertEquals(1, cars.size());
        assertEquals(CarStatus.ACTIVE, cars.get(0).status());
    }

    @Test
    void searchCarsShouldApplyFilters() {
        when(carRepository.findByStatus(CarStatus.ACTIVE)).thenReturn(List.of(CatalogTestDataFactory.activeCar()));
        SearchCarRequest request = CatalogTestDataFactory.brandSearchRequest();

        List<CarResponse> cars = catalogService.searchCars(request);

        assertEquals(1, cars.size());
        assertEquals("Toyota", cars.get(0).brand());
    }

    @Test
    void updateCarShouldThrowWhenCarMissing() {
        UUID carId = UUID.randomUUID();
        UpdateCarRequest request = CatalogTestDataFactory.validUpdateRequest();
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> catalogService.updateCar(carId, request));

        assertTrue(exception.getMessage().contains("Car not found"));
    }

    @Test
    void deleteCarShouldMarkAsDiscontinued() {
        UUID carId = UUID.randomUUID();
        Car car = CatalogTestDataFactory.activeCar();
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        catalogService.deleteCar(carId);

        assertEquals(CarStatus.DISCONTINUED, car.getStatus());
        verify(carRepository).save(car);
    }
}
