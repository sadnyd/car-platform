package com.carplatform.order.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component("orderDependencies")
public class OrderDependenciesHealthIndicator implements HealthIndicator {

    private final WebClient.Builder webClientBuilder;
    private final String inventoryBaseUrl;
    private final String catalogBaseUrl;

    public OrderDependenciesHealthIndicator(
            WebClient.Builder webClientBuilder,
            @Value("${services.inventory.base-url:http://localhost:8082}") String inventoryBaseUrl,
            @Value("${services.catalog.base-url:http://localhost:8081}") String catalogBaseUrl) {
        this.webClientBuilder = webClientBuilder;
        this.inventoryBaseUrl = inventoryBaseUrl;
        this.catalogBaseUrl = catalogBaseUrl;
    }

    @Override
    public Health health() {
        boolean inventoryUp = checkHealth(inventoryBaseUrl + "/actuator/health");
        boolean catalogUp = checkHealth(catalogBaseUrl + "/actuator/health");

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("inventory", inventoryUp ? "UP" : "DOWN");
        details.put("catalog", catalogUp ? "UP" : "DOWN");

        if (inventoryUp && catalogUp) {
            return Health.up().withDetails(details).build();
        }

        return Health.down().withDetails(details).build();
    }

    private boolean checkHealth(String healthUrl) {
        try {
            return Boolean.TRUE.equals(webClientBuilder.build()
                    .get()
                    .uri(healthUrl)
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getStatusCode().is2xxSuccessful())
                    .timeout(Duration.ofSeconds(1))
                    .onErrorReturn(false)
                    .block());
        } catch (Exception exception) {
            return false;
        }
    }
}
