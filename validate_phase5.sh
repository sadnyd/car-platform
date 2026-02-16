#!/bin/bash

# PHASE 5 VALIDATION SCRIPT
echo "========================================="
echo "PHASE 5: Inter-Service Communication"
echo "Validation Check"
echo "========================================="
echo ""

WORKSPACE="/c/Users/sadny/Documents/GitHub/git/car-platform"
PASSED=0
FAILED=0

# Check function
check_file() {
    if [ -f "$1" ]; then
        echo "✅ Found: $1"
        ((PASSED++))
    else
        echo "❌ MISSING: $1"
        ((FAILED++))
    fi
}

# Check configuration
echo "Checking Configuration Files..."
check_file "$WORKSPACE/PHASE_5_COMMUNICATION_CONTRACT.md"
check_file "$WORKSPACE/PHASE_5_IMPLEMENTATION_SUMMARY.md"
check_file "$WORKSPACE/PHASE_5_COMPLETION_GUIDE.md"
check_file "$WORKSPACE/order-service/src/main/resources/application.yaml"
echo ""

# Check WebClient configuration
echo "Checking WebClient Configuration..."
check_file "$WORKSPACE/order-service/src/main/java/com/carplatform/order/config/WebClientConfig.java"
echo ""

# Check clients
echo "Checking Service Clients..."
check_file "$WORKSPACE/order-service/src/main/java/com/carplatform/order/client/InventoryServiceClient.java"
check_file "$WORKSPACE/order-service/src/main/java/com/carplatform/order/client/CatalogServiceClient.java"
echo ""

# Check DTOs
echo "Checking DTOs..."
check_file "$WORKSPACE/order-service/src/main/java/com/carplatform/order/dto/InventoryAvailabilityResponse.java"
check_file "$WORKSPACE/order-service/src/main/java/com/carplatform/order/dto/InventoryReservationRequest.java"
check_file "$WORKSPACE/order-service/src/main/java/com/carplatform/order/dto/InventoryReservationResponse.java"
check_file "$WORKSPACE/order-service/src/main/java/com/carplatform/order/dto/CarDetailsResponse.java"
check_file "$WORKSPACE/inventory-service/src/main/java/com/carplatform/inventory/dto/AvailabilityCheckResponse.java"
check_file "$WORKSPACE/inventory-service/src/main/java/com/carplatform/inventory/dto/ReservationRequest.java"
check_file "$WORKSPACE/inventory-service/src/main/java/com/carplatform/inventory/dto/ReservationResponse.java"
echo ""

# Check orchestration
echo "Checking Orchestration Layer..."
check_file "$WORKSPACE/order-service/src/main/java/com/carplatform/order/service/OrderOrchestrationService.java"
echo ""

# Check tests
echo "Checking Integration Tests..."
check_file "$WORKSPACE/order-service/src/test/java/com/carplatform/order/service/OrderOrchestrationServiceIntegrationTest.java"
echo ""

# Summary
echo "========================================="
echo "Validation Results:"
echo "✅ Passed: $PASSED"
echo "❌ Failed: $FAILED"
echo "========================================="
echo ""

if [ $FAILED -eq 0 ]; then
    echo "✅ All Phase 5 files are present!"
    echo ""
    echo "Next steps:"
    echo "1. cd $WORKSPACE"
    echo "2. Build services: mvn clean package -DskipTests"
    echo "3. Start services on ports 8082, 8081, 8083"
    echo "4. Test inter-service communication"
    exit 0
else
    echo "❌ Some files are missing. Please check the implementation."
    exit 1
fi
