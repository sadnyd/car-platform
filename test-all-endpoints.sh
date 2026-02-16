#!/bin/bash

##############################################################################
# PHASE 5 COMPREHENSIVE ENDPOINT TESTING
# Tests all endpoints and features with success & failure scenarios
##############################################################################

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
PASS=0
FAIL=0
TOTAL=0

# Base URLs
INVENTORY_BASE="http://localhost:8082"
CATALOG_BASE="http://localhost:8081"
ORDER_BASE="http://localhost:8083"

##############################################################################
# UTILITY FUNCTIONS
##############################################################################

log_test() {
    echo -e "${BLUE}[TEST $TOTAL]${NC} $1"
}

log_pass() {
    echo -e "${GREEN}✓ PASS${NC}: $1"
    ((PASS++))
    ((TOTAL++))
}

log_fail() {
    echo -e "${RED}✗ FAIL${NC}: $1"
    ((FAIL++))
    ((TOTAL++))
}

test_endpoint() {
    local method=$1
    local url=$2
    local data=$3
    local expected_code=$4
    local test_name=$5
    
    log_test "$test_name"
    
    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi
    
    http_code=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "$expected_code" ]; then
        log_pass "HTTP $http_code (expected) - $test_name"
        echo "Response: $body" | head -c 200
        echo ""
    else
        log_fail "HTTP $http_code (expected $expected_code) - $test_name"
        echo "Response: $body" | head -c 200
        echo ""
    fi
}

##############################################################################
# DATA SETUP
##############################################################################

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}PHASE 5 COMPREHENSIVE ENDPOINT TESTING${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# Generate unique IDs (using pseudo-UUIDs in bash)
CAR_ID="12345678-1234-1234-1234-123456789001"
USER_ID="12345678-1234-1234-1234-123456789002"
ORDER_ID="12345678-1234-1234-1234-123456789003"
NONEXISTENT_ID="00000000-0000-0000-0000-000000000000"

echo -e "${YELLOW}Test IDs:${NC}"
echo "  CAR_ID: $CAR_ID"
echo "  USER_ID: $USER_ID"
echo "  ORDER_ID: $ORDER_ID"
echo ""

##############################################################################
# SECTION 1: INVENTORY SERVICE TESTS
##############################################################################

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}SECTION 1: INVENTORY SERVICE ENDPOINTS${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

### Test 1.1: Check Availability - Success (car exists)
test_endpoint "GET" "$INVENTORY_BASE/inventory/check-availability/$CAR_ID" "" "200" \
    "Check availability - car exists"
echo ""

### Test 1.2: Check Availability - Failure (car not found)
test_endpoint "GET" "$INVENTORY_BASE/inventory/check-availability/$NONEXISTENT_ID" "" "404" \
    "Check availability - car not found"
echo ""

### Test 1.3: Reserve Inventory - Success (happy path)
RESERVE_PAYLOAD=$(cat <<EOF
{
  "carId": "$CAR_ID",
  "orderId": "$ORDER_ID",
  "units": 1
}
EOF
)
test_endpoint "POST" "$INVENTORY_BASE/inventory/reserve" "$RESERVE_PAYLOAD" "201" \
    "Reserve inventory - success"
echo ""

### Test 1.4: Reserve Inventory - Failure (insufficient stock)
RESERVE_EXCESSIVE=$(cat <<EOF
{
  "carId": "$CAR_ID",
  "orderId": "12345678-1234-1234-1234-123456789004",
  "units": 100
}
EOF
)
test_endpoint "POST" "$INVENTORY_BASE/inventory/reserve" "$RESERVE_EXCESSIVE" "409" \
    "Reserve inventory - insufficient stock"
echo ""

### Test 1.5: Reserve Inventory - Failure (invalid car ID)
RESERVE_INVALID=$(cat <<EOF
{
  "carId": "$NONEXISTENT_ID",
  "orderId": "$ORDER_ID",
  "units": 1
}
EOF
)
test_endpoint "POST" "$INVENTORY_BASE/inventory/reserve" "$RESERVE_INVALID" "404" \
    "Reserve inventory - car not found"
echo ""

##############################################################################
# SECTION 2: CATALOG SERVICE TESTS
##############################################################################

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}SECTION 2: CATALOG SERVICE ENDPOINTS${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

### Test 2.1: Get Car Details - Success
test_endpoint "GET" "$CATALOG_BASE/catalog/cars/$CAR_ID" "" "200" \
    "Get car details - car exists"
echo ""

### Test 2.2: Get Car Details - Failure (not found)
test_endpoint "GET" "$CATALOG_BASE/catalog/cars/$NONEXISTENT_ID" "" "404" \
    "Get car details - car not found"
echo ""

### Test 2.3: List All Cars
test_endpoint "GET" "$CATALOG_BASE/catalog/cars" "" "200" \
    "List all cars"
echo ""

##############################################################################
# SECTION 3: ORDER SERVICE TESTS (WITH ORCHESTRATION)
##############################################################################

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}SECTION 3: ORDER SERVICE ENDPOINTS (WITH ORCHESTRATION)${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

### Test 3.1: Create Order - Success (all services respond)
echo -e "${YELLOW}Test 3.1: Order creation with full inter-service communication${NC}"
CREATE_ORDER_PAYLOAD=$(cat <<EOF
{
  "carId": "$CAR_ID",
  "userId": "$USER_ID",
  "reservationExpiryMinutes": 1440
}
EOF
)

ORDER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$ORDER_BASE/orders" \
    -H "Content-Type: application/json" \
    -d "$CREATE_ORDER_PAYLOAD")

ORDER_HTTP_CODE=$(echo "$ORDER_RESPONSE" | tail -n 1)
ORDER_BODY=$(echo "$ORDER_RESPONSE" | head -n -1)

if [ "$ORDER_HTTP_CODE" = "201" ]; then
    log_pass "HTTP 201 - Order created successfully"
    echo "Response: $ORDER_BODY"
    CREATED_ORDER_ID=$(echo "$ORDER_BODY" | grep -o '"orderId":"[^"]*' | cut -d'"' -f4)
else
    log_fail "HTTP $ORDER_HTTP_CODE (expected 201) - Order creation failed"
    echo "Response: $ORDER_BODY"
fi
echo ""

### Test 3.2: Create Order - Failure (car not found in inventory)
echo -e "${YELLOW}Test 3.2: Creating order for non-existent car${NC}"
CREATE_ORDER_NOT_FOUND=$(cat <<EOF
{
  "carId": "$NONEXISTENT_ID",
  "userId": "$USER_ID",
  "reservationExpiryMinutes": 1440
}
EOF
)
test_endpoint "POST" "$ORDER_BASE/orders" "$CREATE_ORDER_NOT_FOUND" "404" \
    "Create order - car not found in inventory"
echo ""

### Test 3.3: Get Order - Success
if [ ! -z "$CREATED_ORDER_ID" ] && [ "$CREATED_ORDER_ID" != "null" ]; then
    test_endpoint "GET" "$ORDER_BASE/orders/$CREATED_ORDER_ID" "" "200" \
        "Get order - order exists"
    echo ""
fi

### Test 3.4: Get Order - Failure (not found)
test_endpoint "GET" "$ORDER_BASE/orders/$NONEXISTENT_ID" "" "404" \
    "Get order - order not found"
echo ""

### Test 3.5: List All Orders
test_endpoint "GET" "$ORDER_BASE/orders" "" "200" \
    "List all orders"
echo ""

### Test 3.6: Get User Orders
test_endpoint "GET" "$ORDER_BASE/orders/user/$USER_ID" "" "200" \
    "Get user orders"
echo ""

##############################################################################
# SECTION 4: ORDER STATUS & LIFECYCLE TESTS
##############################################################################

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}SECTION 4: ORDER STATUS & LIFECYCLE${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

if [ ! -z "$CREATED_ORDER_ID" ] && [ "$CREATED_ORDER_ID" != "null" ]; then
    ### Test 4.1: Update Order Status
    UPDATE_STATUS_PAYLOAD=$(cat <<EOF
{
  "status": "CONFIRMED"
}
EOF
)
    test_endpoint "PUT" "$ORDER_BASE/orders/$CREATED_ORDER_ID/status" "$UPDATE_STATUS_PAYLOAD" "200" \
        "Update order status to CONFIRMED"
    echo ""

    ### Test 4.2: Cancel Order
    test_endpoint "POST" "$ORDER_BASE/orders/$CREATED_ORDER_ID/cancel" "" "200" \
        "Cancel order"
    echo ""
fi

##############################################################################
# SECTION 5: VALIDATE INTER-SERVICE COMMUNICATION
##############################################################################

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}SECTION 5: INTER-SERVICE COMMUNICATION VALIDATION${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

echo -e "${YELLOW}Verifying that order cannot be created without inventory:${NC}"
UNVALIDATED_ORDER=$(cat <<EOF
{
  "carId": "$NONEXISTENT_ID",
  "userId": "$USER_ID",
  "reservationExpiryMinutes": 1440
}
EOF
)

UNVALIDATED_RESPONSE=$(curl -s -X POST "$ORDER_BASE/orders" \
    -H "Content-Type: application/json" \
    -d "$UNVALIDATED_ORDER" -w "\n%{http_code}")

UNVALIDATED_CODE=$(echo "$UNVALIDATED_RESPONSE" | tail -n 1)
if [ "$UNVALIDATED_CODE" = "404" ]; then
    log_pass "Order correctly rejected (HTTP 404) - inventory validation working"
else
    log_fail "Order should have been rejected (got HTTP $UNVALIDATED_CODE)"
fi
echo ""

##############################################################################
# TEST SUMMARY
##############################################################################

echo ""
echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}TEST SUMMARY${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""
echo "Total Tests: $TOTAL"
echo -e "${GREEN}Passed: $PASS${NC}"
echo -e "${RED}Failed: $FAIL${NC}"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✓ ALL TESTS PASSED!${NC}"
    exit 0
else
    echo -e "${RED}✗ SOME TESTS FAILED!${NC}"
    exit 1
fi
