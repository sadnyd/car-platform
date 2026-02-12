package com.carplatform.inventory.dto;

import java.time.LocalDateTime;

public record PingResponse(
        String service,
        String status,
        LocalDateTime timestamp) {
}