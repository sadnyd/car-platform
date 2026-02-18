package com.carplatform.order.model;

/**
 * Order Status Lifecycle
 * 
 * CREATED
 * └─→ INVENTORY_RESERVED (after successful inventory check & reservation)
 * └─→ CONFIRMED (after cart details fetched, ready for payment)
 * └─→ PROCESSING (payment processed)
 * 
 * Any state → CANCELLED (user cancels)
 * Any state → FAILED (system error during processing)
 */
public enum OrderStatus {
    CREATED, // Initial state: order created but not validated
    INVENTORY_RESERVED, // Inventory check passed & stock reserved
    CONFIRMED, // Car details fetched, order confirmed
    PROCESSING, // Payment processing
    COMPLETED, // Order completed successfully
    CANCELLED, // Order cancelled by user
    FAILED // Order failed (system or business error)
}
