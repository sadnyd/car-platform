package com.carplatform.catalog.controller;

import com.carplatform.catalog.dto.CarResponse;
import com.carplatform.catalog.dto.CreateCarRequest;
import com.carplatform.catalog.exception.GlobalExceptionHandler;
import com.carplatform.catalog.model.CarStatus;
import com.carplatform.catalog.model.FuelType;
import com.carplatform.catalog.model.TransmissionType;
import com.carplatform.catalog.service.CatalogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CarsController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CarsController API Tests")
class CarsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogService catalogService;

    @Test
    void createCarShouldReturn201() throws Exception {
        UUID carId = UUID.randomUUID();
        CarResponse response = new CarResponse(
                carId,
                "Toyota",
                "Corolla",
                "XLE",
                2024,
                FuelType.PETROL,
                TransmissionType.AUTOMATIC,
                BigDecimal.valueOf(25000),
                CarStatus.ACTIVE,
                "desc",
                Instant.now());

        when(catalogService.createCar(any(CreateCarRequest.class))).thenReturn(response);

        mockMvc.perform(post("/catalog")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"brand\":\"Toyota\",\"model\":\"Corolla\",\"variant\":\"XLE\",\"manufacturingYear\":2024,\"fuelType\":\"PETROL\",\"transmissionType\":\"AUTOMATIC\",\"price\":25000,\"description\":\"desc\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carId").value(carId.toString()));
    }

    @Test
    void createCarShouldReturn400ForInvalidPayload() throws Exception {
        mockMvc.perform(post("/catalog")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"brand\":\"\",\"model\":\"Corolla\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCarShouldReturn404WhenMissing() throws Exception {
        UUID carId = UUID.randomUUID();
        when(catalogService.getCarById(carId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/catalog/{carId}", carId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }
}
