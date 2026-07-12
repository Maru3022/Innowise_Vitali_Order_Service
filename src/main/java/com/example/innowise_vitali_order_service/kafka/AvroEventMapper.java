package com.example.innowise_vitali_order_service.kafka;

import com.example.events.OrderCreatedEvent;

import java.math.BigDecimal;

public class AvroEventMapper {

    public OrderCreatedEvent toOrderCreatedAvro(com.example.innowise_vitali_order_service.kafka.OrderCreatedEvent domainEvent) {
        OrderCreatedEvent avroEvent = new OrderCreatedEvent();
        avroEvent.setOrderId(domainEvent.getOrderId());
        avroEvent.setUserId(domainEvent.getUserId());
        avroEvent.setTotalPrice(domainEvent.getTotalPrice());
        return avroEvent;
    }

    public PaymentEvent toPaymentDomain(com.example.events.PaymentEvent avroEvent) {
        return PaymentEvent.builder()
                .paymentId(avroEvent.getPaymentId())
                .orderId(avroEvent.getOrderId())
                .userId(avroEvent.getUserId())
                .status(PaymentStatus.valueOf(avroEvent.getStatus().name()))
                .build();
    }
}
