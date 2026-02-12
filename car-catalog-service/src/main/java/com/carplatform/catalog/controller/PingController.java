package com.carplatform.catalog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.carplatform.catalog.dto.PingResponse;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/")
public class PingController {

    @GetMapping("/ping")
    public PingResponse ping() {

        return new PingResponse("car-catalog-service", "UP", LocalDateTime.now());

    }

    @GetMapping("/test-error")
    public String testError() {
        throw new RuntimeException("Simulated failure");
    }

}
