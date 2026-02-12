package com.carplatform.inventory.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.carplatform.inventory.dto.PingResponse;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/")
public class PingController {

    @GetMapping("/ping")
    public PingResponse ping() {

        return new PingResponse("inventory-service", "UP", LocalDateTime.now());

    }
}
