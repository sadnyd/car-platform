package com.carplatform.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger Configuration
 * 
 * 
 * Configures Springdoc-OpenAPI to generate OpenAPI 3.0 specification for the
 * API.
 * Auto-generates Swagger UI accessible at:
 * - /swagger-ui.html (UI)
 * - /v3/api-docs (JSON spec)
 * - /v3/api-docs.yaml (YAML spec)
 */
@Configuration
public class OpenAPIConfiguration {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Car Platform - Aggregation API")
                                                .version("1.0.0")
                                                .description("Client-friendly aggregated APIs combining data from multiple microservices\n\n"
                                                                +
                                                                "## Phase 6: Aggregation Pattern\n\n" +
                                                                "This API provides two primary aggregation endpoints:\n\n"
                                                                +
                                                                "### 1. Car Details with Availability\n" +
                                                                "- Combines car catalog data with real-time inventory availability\n"
                                                                +
                                                                "- Returns single car with complete information\n" +
                                                                "- Graceful degradation if inventory service unavailable (returns UNKNOWN status)\n\n"
                                                                +
                                                                "### 2. Car Listing with Pagination\n" +
                                                                "- Returns paginated list of all cars\n" +
                                                                "- Includes availability flags for each car\n" +
                                                                "- Supports pagination (page size 1-100, default 20)\n\n"
                                                                +
                                                                "## Failure Handling\n\n" +
                                                                "- **Catalog Service Down**: HTTP 503 - Cannot provide any data\n"
                                                                +
                                                                "- **Inventory Service Down**: HTTP 200/206 - Provides car data with UNKNOWN availability\n"
                                                                +
                                                                "- **Invalid Parameters**: HTTP 400 - Client error\n" +
                                                                "- **Not Found**: HTTP 404 - Car not found in catalog\n\n"
                                                                +
                                                                "## Performance\n\n" +
                                                                "- Catalog timeout: 5 seconds (no retry) - fast fail\n"
                                                                +
                                                                "- Inventory timeout: 3 seconds with 2 retries - resilient\n"
                                                                +
                                                                "- Circuit breaker: 50% failure threshold, 30s open state\n\n"
                                                                +
                                                                "## Documentation\n\n" +
                                                                "- Swagger UI: http://localhost:8080/swagger-ui.html\n"
                                                                +
                                                                "- OpenAPI JSON: http://localhost:8080/v3/api-docs\n" +
                                                                "- OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml\n")
                                                .contact(new Contact()
                                                                .name("Car Platform Team")
                                                                .email("support@carplatform.com")
                                                                .url("https://carplatform.com"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8080")
                                                                .description("Development Server"),
                                                new Server()
                                                                .url("https://api.carplatform.com")
                                                                .description("Production Server")));
        }
}
