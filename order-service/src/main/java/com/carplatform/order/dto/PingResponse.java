package com.carplatform.order.dto;

import java.time.LocalDateTime;

public record PingResponse(
        String service,
        String status,
        LocalDateTime timestamp) {
}