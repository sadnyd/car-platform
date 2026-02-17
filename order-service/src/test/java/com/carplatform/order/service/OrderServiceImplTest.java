package com.carplatform.order.service;

import com.carplatform.order.dto.CreateOrderRequest;
import com.carplatform.order.dto.OrderResponse;
import com.carplatform.order.dto.UpdateOrderStatusRequest;
import com.carplatform.order.model.Order;
import com.carplatform.order.model.OrderStatus;
import com.carplatform.order.repository.OrderRepository;
import com.carplatform.order.testdata.OrderTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Business Rule Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrderShouldPersistCreatedOrder() {
        CreateOrderRequest request = OrderTestDataFactory.validCreateOrderRequest();
        Order persisted = OrderTestDataFactory.createdOrder();

        when(orderRepository.save(any(Order.class))).thenReturn(persisted);

        OrderResponse response = orderService.createOrder(request);

        assertEquals(OrderStatus.CREATED, response.status());
        assertEquals(persisted.getOrderId(), response.orderId());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertEquals(request.carId(), captor.getValue().getCarId());
        assertEquals(request.userId(), captor.getValue().getUserId());
    }

    @Test
    void updateOrderStatusShouldRejectTerminalStateUpdate() {
        Order completed = OrderTestDataFactory.completedOrder();
        when(orderRepository.findById(completed.getOrderId())).thenReturn(Optional.of(completed));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.updateOrderStatus(completed.getOrderId(),
                        new UpdateOrderStatusRequest(OrderStatus.PROCESSING)));

        assertTrue(exception.getMessage().contains("terminal state"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateToProcessingShouldFailWhenReservationExpired() {
        Order order = OrderTestDataFactory.createdOrder();
        order.setReservationExpiry(Instant.now().minusSeconds(10));
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.updateOrderStatus(order.getOrderId(),
                        new UpdateOrderStatusRequest(OrderStatus.PROCESSING)));

        assertTrue(exception.getMessage().contains("Reservation has expired"));
    }

    @Test
    void cancelOrderShouldWorkForCreatedStatus() {
        Order order = OrderTestDataFactory.createdOrder();
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.cancelOrder(order.getOrderId());

        assertEquals(OrderStatus.CANCELLED, response.status());
    }

    @Test
    void cancelOrderShouldRejectNonCancelableState() {
        Order order = OrderTestDataFactory.createdOrder();
        order.setStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.cancelOrder(order.getOrderId()));

        assertTrue(exception.getMessage().contains("Can only cancel"));
    }

    @Test
    void isReservationExpiredShouldReturnRepositoryResult() {
        Order order = OrderTestDataFactory.createdOrder();
        order.setReservationExpiry(Instant.now().minusSeconds(1));
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));

        assertTrue(orderService.isReservationExpired(order.getOrderId()));
    }
}
