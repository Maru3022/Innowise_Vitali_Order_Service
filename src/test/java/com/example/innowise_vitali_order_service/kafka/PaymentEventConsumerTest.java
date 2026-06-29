package com.example.innowise_vitali_order_service.kafka;

import com.example.innowise_vitali_order_service.entity.Order;
import com.example.innowise_vitali_order_service.entity.OrderStatus;
import com.example.innowise_vitali_order_service.exception.OrderNotFoundException;
import com.example.innowise_vitali_order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentEventConsumer consumer;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setUserId(42L);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.TEN);
    }

    @Test
    void consume_shouldSetStatusPaid_whenPaymentSuccess() {
        PaymentEvent event = buildEvent("SUCCESS");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        consumer.consume(event, "CREATE_PAYMENT", 0L);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void consume_shouldSetStatusCancelled_whenPaymentFailed() {
        PaymentEvent event = buildEvent("FAILED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        consumer.consume(event, "CREATE_PAYMENT", 1L);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void consume_shouldThrowOrderNotFoundException_whenOrderNotFound() {
        PaymentEvent event = buildEvent("SUCCESS");
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumer.consume(event, "CREATE_PAYMENT", 2L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("1");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void consume_shouldSkipSave_whenStatusIsUnknown() {
        PaymentEvent event = buildEvent("PENDING");

        consumer.consume(event, "CREATE_PAYMENT", 3L);

        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void consume_shouldSkipSave_whenStatusIsNull() {
        PaymentEvent event = buildEvent(null);

        consumer.consume(event, "CREATE_PAYMENT", 4L);

        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    private PaymentEvent buildEvent(String status) {
        return PaymentEvent.builder()
                .paymentId("pay-001")
                .orderId(1L)
                .status(status)
                .amount(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
