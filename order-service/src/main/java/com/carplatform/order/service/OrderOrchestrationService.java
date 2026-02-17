package com.carplatform.order.service;

import com.carplatform.order.client.InventoryServiceClient;
import com.carplatform.order.client.CatalogServiceClient;
import com.carplatform.order.dto.CarDetailsResponse;
import com.carplatform.order.dto.CreateOrderRequest;
import com.carplatform.order.dto.InventoryAvailabilityResponse;
import com.carplatform.order.dto.InventoryReservationRequest;
import com.carplatform.order.dto.InventoryReservationResponse;
import com.carplatform.order.dto.OrderResponse;
import com.carplatform.order.model.Order;
import com.carplatform.order.model.OrderStatus;
import com.carplatform.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
// import java.util.concurrent.atomic.AtomicReference;

/**
 * Order Orchestration Service - PHASE 5
 * 
 * Orchestrates inter-service communication for order creation:
 * 1. Check inventory availability via Inventory Service
 * 2. Reserve inventory if available
 * 3. Fetch car details from Catalog Service
 * 4. Create order with INVENTORY_RESERVED status
 * 
 * PHASE 5.6 - Transaction Boundaries:
 * 
 * KEY PRINCIPLE: No @Transactional across service calls
 * 
 * Transaction Model:
 * ├─ Inventory.checkAvailability() → Inventory Service TX (independent)
 * ├─ Inventory.reserve() → Inventory Service TX (independent)
 * ├─ Catalog.getCarDetails() → Catalog Service TX (independent)
 * └─ Order.create() → Order Service TX (THIS SERVICE)
 * 
 * Critical Points:
 * - Each service manages its own database transaction
 * - Order Service does NOT assume Inventory commit
 * - If Inventory fails, Order is NOT created
 * - If Catalog fails, Order is still created (best effort for pricing)
 * - NO distributed 2-phase commit (would be complex & slow)
 * - NO assumption that Inventory will rollback if Order fails
 * 
 * Compensating Actions (Not yet implemented):
 * - If Order creation fails after reservation, we need to release inventory
 * - This would be async via events (Phase 5.X - Event-driven)
 * - For now, manual cleanup or TTL expiry handles this
 * 
 * Uses WebClient for non-blocking async calls (future Phase 5.12)
 * Currently using blocking calls for Phase 5.5 (can be made reactive later)
 */
@Slf4j
@Service
public class OrderOrchestrationService {

    @Autowired
    private InventoryServiceClient inventoryServiceClient;

    @Autowired
    private CatalogServiceClient catalogServiceClient;

    @Autowired
    private OrderRepository orderRepository;

        @Autowired
        private MeterRegistry meterRegistry;

        private Counter ordersCreatedCounter;
        private Counter ordersFailedCounter;
        private Counter inventoryReservationFailureCounter;
        private Timer catalogLookupLatencyTimer;

        @jakarta.annotation.PostConstruct
        public void initMetrics() {
        ordersCreatedCounter = Counter.builder("carplatform.orders.created.count")
            .description("Total successful orders created")
            .register(meterRegistry);
        ordersFailedCounter = Counter.builder("carplatform.orders.failed.count")
            .description("Total failed order creation attempts")
            .register(meterRegistry);
        inventoryReservationFailureCounter = Counter.builder("carplatform.inventory.reservation.failures.count")
            .description("Inventory reservation failures during order creation")
            .register(meterRegistry);
        catalogLookupLatencyTimer = Timer.builder("carplatform.catalog.lookup.latency")
            .description("Latency for catalog lookup in order flow")
            .register(meterRegistry);
        }

    /**
     * Create order with inventory validation and reservation
     * 
     * PHASE 5.6 Note:
     * - This method is marked @Transactional to ensure Order creation is atomic
     * - But external service calls have their own independent transactions
     * - If external call fails, we don't create the order
     * - If order creation fails after external calls, we log a warning but
     * don't automatically compensate (would require event-driven compensation)
     * 
     * Workflow:
     * 1. Check inventory availability
     * - If not available → Return error, don't create order
     * - If available → Proceed to step 2
     * 
     * 2. Reserve inventory
     * - If reservation fails → Return error, don't create order
     * - If successful → Proceed to step 3
     * 
     * 3. Fetch car details
     * - If catalog unreachable → Log warning, continue (best effort)
     * - Store price from catalog in order
     * 
     * 4. Create order
     * - Status: INVENTORY_RESERVED
     * - Store reservation ID from step 2
     * - Store car price from step 3
     * 
     * @param request CreateOrderRequest with carId, userId,
     *                reservationExpiryMinutes
     * @return OrderResponse with INVENTORY_RESERVED status
     * @throws Exception if any critical step fails
     */
    @Transactional
    public OrderResponse createOrderWithInventoryValidation(CreateOrderRequest request) {
        log.info("Starting order orchestration - car: {}, user: {}", request.carId(), request.userId());

        String carId = request.carId().toString();

        // Step 1: Check availability (INDEPENDENT TX - Inventory Service)
        log.debug("Step 1: Checking inventory availability for car {}", carId);
        InventoryAvailabilityResponse availabilityResponse = inventoryServiceClient
            .guardedCheckAvailability(carId)
                .block(); // WARNING: blocking call (Phase 5.12 will make this reactive)

        if (availabilityResponse == null || !availabilityResponse.isAvailable()) {
            log.warn("Inventory not available for car: {}", carId);
            ordersFailedCounter.increment();
            throw new OrderCreationException("Inventory not available for car: " + carId,
                    "INVENTORY_UNAVAILABLE");
        }

        log.info("Inventory available - car: {}, available units: {}",
                carId, availabilityResponse.getAvailableUnits());

        // Step 2: Reserve inventory (INDEPENDENT TX - Inventory Service)
        log.debug("Step 2: Reserving inventory for car {}", carId);

        String tempOrderId = "temp-" + request.userId().toString(); // Temporary ID for reservation
        InventoryReservationRequest reservationRequest = new InventoryReservationRequest(
                carId,
                tempOrderId,
                1 // Reserve 1 unit
        );

        InventoryReservationResponse reservationResponse = inventoryServiceClient
            .guardedReserveInventory(reservationRequest)
                .block(); // WARNING: blocking call (Phase 5.12 will make this reactive)

        if (reservationResponse == null || reservationResponse.getErrorCode() != null) {
            log.error("Failed to reserve inventory - car: {}, error: {}",
                    carId, reservationResponse != null ? reservationResponse.getErrorCode() : "unknown");
            ordersFailedCounter.increment();
            inventoryReservationFailureCounter.increment();
            throw new OrderCreationException("Failed to reserve inventory for car: " + carId,
                    reservationResponse != null ? reservationResponse.getErrorCode() : "RESERVATION_FAILED");
        }

        log.info("Inventory reserved - reservation ID: {}, units remaining: {}",
                reservationResponse.getReservationId(), reservationResponse.getUnitsRemaining());

        // Step 3: Fetch car details (INDEPENDENT TX - Catalog Service, best effort)
        log.debug("Step 3: Fetching car details from catalog");
        BigDecimal carPrice = BigDecimal.valueOf(0); // Default price
        Timer.Sample lookupSample = Timer.start(meterRegistry);

        try {
            CarDetailsResponse carDetails = catalogServiceClient
                .guardedGetCarDetails(carId)
                    .block(); // WARNING: blocking call

            if (carDetails != null && carDetails.getPrice() > 0) {
                carPrice = BigDecimal.valueOf(carDetails.getPrice());
                log.info("Car details fetched - price: {}", carPrice);
            } else {
                log.warn("Car details unavailable, using default price");
            }
        } catch (Exception e) {
            log.warn("Failed to fetch car details (continuing without price): {}", e.getMessage());
            // Continue anyway - price can be fetched later or user pays at checkout
        } finally {
            lookupSample.stop(catalogLookupLatencyTimer);
        }

        // Step 4: Create order (LOCAL TX - Order Service)
        log.debug("Step 4: Creating order in database");
        Instant reservationExpiry = Instant.now().plusSeconds(
                request.reservationExpiryMinutes() * 60L);

        Order order = new Order();
        order.setCarId(request.carId());
        order.setUserId(request.userId());
        order.setPriceAtPurchase(carPrice);
        order.setStatus(OrderStatus.INVENTORY_RESERVED); // <-- State reflects successful inventory reservation
        order.setOrderDate(Instant.now());
        order.setReservationExpiry(reservationExpiry);
        order.setInventoryReservationId(reservationResponse.getReservationId()); // <-- Link to inventory
        order.setLastUpdated(Instant.now());

        Order savedOrder = orderRepository.save(order);
        ordersCreatedCounter.increment();

        log.info("Order created successfully - order ID: {}, status: INVENTORY_RESERVED",
                savedOrder.getOrderId());

        return mapToResponse(savedOrder);
    }

    /**
     * Convert Order model to OrderResponse DTO
     */
    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getCarId(),
                order.getUserId(),
                order.getPriceAtPurchase(),
                order.getStatus(),
                order.getOrderDate(),
                order.getReservationExpiry(),
                order.getLastUpdated());
    }

    /**
     * Custom exception for order creation failures
     */
    public static class OrderCreationException extends RuntimeException {
        private final String errorCode;

        public OrderCreationException(String message, String errorCode) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
