package com.example.innowise_vitali_order_service.kafka;

import com.example.innowise_vitali_order_service.entity.Order;
import com.example.innowise_vitali_order_service.entity.OrderStatus;
import com.example.innowise_vitali_order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        consumer.consume(buildEvent(PaymentStatus.SUCCESS));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void consume_shouldSetStatusCancelled_whenPaymentFailed() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        consumer.consume(buildEvent(PaymentStatus.FAILED));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void consume_shouldSetStatusCancelled_whenPaymentRejected() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        consumer.consume(buildEvent(PaymentStatus.REJECTED));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void consume_shouldSkip_whenOrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        consumer.consume(buildEvent(PaymentStatus.SUCCESS));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void consume_shouldSkip_whenStatusIsPending() {
        consumer.consume(buildEvent(PaymentStatus.PENDING));

        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void consume_shouldSkip_whenOrderIdIsInvalidFormat() {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId("pay-001")
                .orderId("not-a-number")
                .userId("42")
                .status(PaymentStatus.SUCCESS)
                .build();

        consumer.consume(event);

        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    private PaymentEvent buildEvent(PaymentStatus status) {
        return PaymentEvent.builder()
                .paymentId("pay-001")
                .orderId("1")
                .userId("42")
                .status(status)
                .build();
    }
}
