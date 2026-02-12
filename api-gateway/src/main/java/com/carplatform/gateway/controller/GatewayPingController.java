package com.carplatform.gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway")
public class GatewayPingController {

    @GetMapping("/ping")
    public String ping() {
        return "API Gateway is running";
    }

}
