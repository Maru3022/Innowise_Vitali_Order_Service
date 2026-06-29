package com.example.innowise_vitali_order_service.kafka;

import java.math.BigDecimal;

/**
 * Event published to the CREATE_PAYMENT Kafka topic after an order is saved.
 * Payment Service consumes this event to initiate payment processing.
 */
public record OrderCreatedEvent(
        String orderId,
        String userId,
        BigDecimal totalPrice
) {}
