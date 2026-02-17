package com.carplatform.catalog.integration;

import com.carplatform.catalog.repository.CarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Catalog Full Flow Integration Test")
class CatalogFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarRepository carRepository;

    @Test
    void shouldCreateListDeleteAndHideCarFromActiveList() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/catalog")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"brand\":\"Toyota\",\"model\":\"Corolla\",\"variant\":\"XLE\",\"manufacturingYear\":2024,\"fuelType\":\"PETROL\",\"transmissionType\":\"AUTOMATIC\",\"price\":25000,\"description\":\"integration\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String createBody = createResult.getResponse().getContentAsString();
        Pattern pattern = Pattern.compile("\"carId\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(createBody);
        assertTrue(matcher.find());
        String carId = matcher.group(1);

        mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/catalog/{carId}", carId))
                .andExpect(status().isNoContent());

        MvcResult listAfterDelete = mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("[]", listAfterDelete.getResponse().getContentAsString());
        assertEquals(1, carRepository.count());
    }
}
