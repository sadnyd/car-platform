package com.carplatform.order.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingContextConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @PostConstruct
    public void setUpMdc() {
        // Add service name to MDC so every log line contains it
        MDC.put("service", applicationName);
    }
}