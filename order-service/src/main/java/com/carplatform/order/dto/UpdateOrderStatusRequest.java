package com.carplatform.order.dto;

import com.carplatform.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull OrderStatus status) {
}
