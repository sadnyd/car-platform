package com.carplatform.gateway.service;

import com.carplatform.gateway.client.CatalogServiceClient;
import com.carplatform.gateway.client.InventoryServiceClient;
import com.carplatform.gateway.dto.CarDetailsAggregatedResponse;
import com.carplatform.gateway.dto.CarListingAggregatedResponse;
import com.carplatform.gateway.dto.CarResponse;
import com.carplatform.gateway.dto.InventoryAvailabilityResponse;
import com.carplatform.gateway.exception.ResourceNotFoundException;
import com.carplatform.gateway.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for AggregationService
 * 
 * 
 * Tests both happy path and failure scenarios:
 * - Successful aggregation with both services responding
 * - Inventory timeout with graceful degradation
 * - Catalog 404 with total failure
 * - Pagination handling
 * - Error scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AggregationService Unit Tests")
class AggregationServiceUnitTest {

        @Mock
        private CatalogServiceClient catalogServiceClient;

        @Mock
        private InventoryServiceClient inventoryServiceClient;

        @InjectMocks
        private AggregationService aggregationService;

        private UUID testCarId;
        private CarResponse testCar;
        private InventoryAvailabilityResponse testInventoryResponse;

        @BeforeEach
        void setUp() {
                testCarId = UUID.randomUUID();

                // Create test car
                testCar = new CarResponse();
                testCar.setId(testCarId);
                testCar.setMake("Tesla");
                testCar.setModel("Model S");
                testCar.setYear(2024);
                testCar.setPrice(89999.99);
                testCar.setColor("Black");

                // Create test inventory response
                testInventoryResponse = new InventoryAvailabilityResponse();
                testInventoryResponse.setCarId(testCarId.toString());
                testInventoryResponse.setStatus("IN_STOCK");
                testInventoryResponse.setTotalUnits(10);
                testInventoryResponse.setAvailableUnits(8);
                testInventoryResponse.setReservedUnits(2);
        }

        // ===================== HAPPY PATH TESTS =====================

        @Test
        @DisplayName("Should return car details with availability when both services respond")
        void testGetCarDetailsWithAvailability_Success() {
                // GIVEN: Both services return valid responses
                when(catalogServiceClient.getCarById(testCarId)).thenReturn(testCar);
                when(inventoryServiceClient.checkAvailability(testCarId.toString()))
                                .thenReturn(testInventoryResponse);

                // WHEN: Getting car details with availability
                CarDetailsAggregatedResponse response = aggregationService
                                .getCarDetailsWithAvailability(testCarId);

                // THEN: Response contains merged data
                assertNotNull(response);
                assertEquals("Tesla", response.getMake());
                assertEquals("Model S", response.getModel());
                assertEquals("IN_STOCK", response.getAvailability().getStatus());
                assertEquals(8, response.getAvailability().getAvailableUnits());
                assertNotNull(response.getMetadata());
                assertEquals(200, response.getMetadata().getAggregationStatus());

                // VERIFY: Both clients were called
                verify(catalogServiceClient, times(1)).getCarById(testCarId);
                verify(inventoryServiceClient, times(1)).checkAvailability(testCarId.toString());
        }

        // ===================== FAILURE SCENARIOS =====================

        @Test
        @DisplayName("Should fail when catalog service returns 404")
        void testGetCarDetailsWithAvailability_CatalogNotFound() {
                // GIVEN: Catalog service throws ResourceNotFoundException
                when(catalogServiceClient.getCarById(testCarId))
                                .thenThrow(new ResourceNotFoundException("Car not found", "Car", testCarId.toString()));

                // WHEN & THEN: Should throw exception (hard fail)
                assertThrows(ResourceNotFoundException.class, () -> {
                        aggregationService.getCarDetailsWithAvailability(testCarId);
                });

                // VERIFY: Inventory service was never called (fast fail)
                verify(inventoryServiceClient, never()).checkAvailability(any());
        }

        @Test
        @DisplayName("Should return degraded response when inventory service unavailable")
        void testGetCarDetailsWithAvailability_InventoryUnavailable() {
                // GIVEN: Catalog succeeds but Inventory throws ServiceUnavailableException
                when(catalogServiceClient.getCarById(testCarId)).thenReturn(testCar);
                when(inventoryServiceClient.checkAvailability(testCarId.toString()))
                                .thenThrow(new ServiceUnavailableException("Service unavailable", "inventory"));

                // WHEN: Getting car details with availability
                CarDetailsAggregatedResponse response = aggregationService
                                .getCarDetailsWithAvailability(testCarId);

                // THEN: Response contains car data but degraded availability
                assertNotNull(response);
                assertEquals("Tesla", response.getMake());
                assertEquals("UNKNOWN", response.getAvailability().getStatus());
                assertTrue(response.getAvailability().getReason()
                                .contains("temporarily unavailable"));
                assertEquals(206, response.getMetadata().getAggregationStatus()); // Partial content
        }

        @Test
        @DisplayName("Should return OUT_OF_STOCK when inventory returns 404")
        void testGetCarDetailsWithAvailability_InventoryNotFound() {
                // GIVEN: Catalog succeeds but inventory throws 404
                when(catalogServiceClient.getCarById(testCarId)).thenReturn(testCar);
                when(inventoryServiceClient.checkAvailability(testCarId.toString()))
                                .thenThrow(new ResourceNotFoundException("Car not in inventory", "Inventory",
                                                testCarId.toString()));

                // WHEN: Getting car details with availability
                CarDetailsAggregatedResponse response = aggregationService
                                .getCarDetailsWithAvailability(testCarId);

                // THEN: Response shows OUT_OF_STOCK (expected for new cars)
                assertNotNull(response);
                assertEquals("OUT_OF_STOCK", response.getAvailability().getStatus());
                assertEquals(200, response.getMetadata().getAggregationStatus());
        }

        // ===================== LISTING TESTS =====================

        @Test
        @DisplayName("Should return paginated car listing with availability")
        void testGetCarListingWithAvailability_Success() {
                // GIVEN: Multiple cars in catalog and inventory responses available
                List<CarResponse> allCars = List.of(testCar, createSecondTestCar(), createThirdTestCar());
                when(catalogServiceClient.listAllCars()).thenReturn(allCars);
                when(inventoryServiceClient.checkAvailability(testCarId.toString()))
                                .thenReturn(testInventoryResponse);
                when(inventoryServiceClient.checkAvailability(any()))
                                .thenReturn(testInventoryResponse);

                // WHEN: Getting car listing (page 1, size 20)
                CarListingAggregatedResponse response = aggregationService
                                .getCarListingWithAvailability(1, 20);

                // THEN: Response contains paginated list with availability
                assertNotNull(response);
                assertEquals(3, response.getCars().size());
                assertEquals(3, response.getPagination().getTotalCount());
                assertEquals(1, response.getPagination().getTotalPages());
                assertEquals(1, response.getPagination().getCurrentPage());
                assertTrue(response.getCars().stream()
                                .allMatch(item -> item.getAvailabilityStatus() != null));
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void testGetCarListingWithAvailability_Pagination() {
                // GIVEN: 5 cars with page size of 2
                List<CarResponse> allCars = List.of(
                                testCar,
                                createSecondTestCar(),
                                createThirdTestCar(),
                                createFourthTestCar(),
                                createFifthTestCar());
                when(catalogServiceClient.listAllCars()).thenReturn(allCars);
                when(inventoryServiceClient.checkAvailability(any()))
                                .thenReturn(testInventoryResponse);

                // WHEN: Getting page 2 with size 2
                CarListingAggregatedResponse response = aggregationService
                                .getCarListingWithAvailability(2, 2);

                // THEN: Response contains correct page
                assertNotNull(response);
                assertEquals(2, response.getCars().size()); // 2 items on this page
                assertEquals(5, response.getPagination().getTotalCount()); // 5 total
                assertEquals(3, response.getPagination().getTotalPages()); // ceil(5/2)
                assertEquals(2, response.getPagination().getCurrentPage());
        }

        @Test
        @DisplayName("Should handle inventory failures gracefully in listing")
        void testGetCarListingWithAvailability_PartialInventoryFailure() {
                // GIVEN: Catalog returns cars but inventory fails for some
                List<CarResponse> allCars = List.of(testCar, createSecondTestCar());
                when(catalogServiceClient.listAllCars()).thenReturn(allCars);
                when(inventoryServiceClient.checkAvailability(testCarId.toString()))
                                .thenReturn(testInventoryResponse);
                when(inventoryServiceClient.checkAvailability(any()))
                                .thenThrow(new ServiceUnavailableException("Service down", "inventory"));

                // WHEN: Getting car listing
                CarListingAggregatedResponse response = aggregationService
                                .getCarListingWithAvailability(1, 20);

                // THEN: Response still returns cars with partial availability info
                assertNotNull(response);
                assertEquals(2, response.getCars().size());
                // First car has availability, second has UNKNOWN (due to service failure)
                assertTrue(response.getCars().stream()
                                .anyMatch(item -> "IN_STOCK".equals(item.getAvailabilityStatus())));
                assertTrue(response.getCars().stream()
                                .anyMatch(item -> "UNKNOWN".equals(item.getAvailabilityStatus())));
        }

        // ===================== VALIDATION TESTS =====================

        @Test
        @DisplayName("Should enforce maximum page size")
        void testGetCarListingWithAvailability_MaxPageSize() {
                // GIVEN: List with many cars
                List<CarResponse> allCars = createManyTestCars(150);
                when(catalogServiceClient.listAllCars()).thenReturn(allCars);
                when(inventoryServiceClient.checkAvailability(any()))
                                .thenReturn(testInventoryResponse);

                // WHEN: Requesting page size larger than max (100)
                CarListingAggregatedResponse response = aggregationService
                                .getCarListingWithAvailability(1, 150);

                // THEN: Response should cap at max size
                assertEquals(100, response.getCars().size()); // Should be capped to 100
                assertEquals(150, response.getPagination().getTotalCount()); // But total is 150
                assertEquals(2, response.getPagination().getTotalPages()); // ceil(150/100)
        }

        // ===================== HELPER METHODS =====================

        private CarResponse createSecondTestCar() {
                CarResponse car = new CarResponse();
                car.setId(UUID.randomUUID());
                car.setMake("BMW");
                car.setModel("X5");
                car.setYear(2024);
                car.setPrice(65000.00);
                car.setColor("White");
                return car;
        }

        private CarResponse createThirdTestCar() {
                CarResponse car = new CarResponse();
                car.setId(UUID.randomUUID());
                car.setMake("Audi");
                car.setModel("A4");
                car.setYear(2024);
                car.setPrice(42000.00);
                car.setColor("Silver");
                return car;
        }

        private CarResponse createFourthTestCar() {
                CarResponse car = new CarResponse();
                car.setId(UUID.randomUUID());
                car.setMake("Mercedes");
                car.setModel("C-Class");
                car.setYear(2024);
                car.setPrice(55000.00);
                car.setColor("Black");
                return car;
        }

        private CarResponse createFifthTestCar() {
                CarResponse car = new CarResponse();
                car.setId(UUID.randomUUID());
                car.setMake("Toyota");
                car.setModel("Camry");
                car.setYear(2024);
                car.setPrice(32000.00);
                car.setColor("Blue");
                return car;
        }

        private List<CarResponse> createManyTestCars(int count) {
                return java.util.stream.IntStream.range(0, count)
                                .mapToObj(i -> {
                                        CarResponse car = new CarResponse();
                                        car.setId(UUID.randomUUID());
                                        car.setMake("Brand" + i);
                                        car.setModel("Model" + i);
                                        car.setYear(2024);
                                        car.setPrice(30000.00 + i * 1000);
                                        car.setColor("Color" + i);
                                        return car;
                                })
                                .toList();
        }
}
