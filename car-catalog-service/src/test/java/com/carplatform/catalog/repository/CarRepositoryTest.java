package com.carplatform.catalog.repository;

import com.carplatform.catalog.model.Car;
import com.carplatform.catalog.model.CarStatus;
import com.carplatform.catalog.testdata.CatalogTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CarRepository JPA Tests")
class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;

    @Test
    void shouldFindByStatus() {
        Car active = CatalogTestDataFactory.activeCar();
        Car discontinued = CatalogTestDataFactory.discontinuedCar();
        carRepository.save(active);
        carRepository.save(discontinued);

        List<Car> activeCars = carRepository.findByStatus(CarStatus.ACTIVE);

        assertEquals(1, activeCars.size());
        assertEquals(CarStatus.ACTIVE, activeCars.get(0).getStatus());
    }

    @Test
    void shouldFindByBrandIgnoreCase() {
        carRepository.save(CatalogTestDataFactory.activeCar());

        List<Car> cars = carRepository.findByBrandIgnoreCase("toyota");

        assertEquals(1, cars.size());
    }

    @Test
    void shouldFindByPriceBetween() {
        Car car = CatalogTestDataFactory.activeCar();
        carRepository.save(car);

        List<Car> cars = carRepository.findByPriceBetween(BigDecimal.valueOf(20000), BigDecimal.valueOf(30000));

        assertEquals(1, cars.size());
    }
}
