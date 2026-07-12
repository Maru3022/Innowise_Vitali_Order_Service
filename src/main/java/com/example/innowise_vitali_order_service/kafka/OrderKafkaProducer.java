package com.example.innowise_vitali_order_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKafkaProducer {

    private final KafkaTemplate<String, com.example.events.OrderCreatedEvent> orderCreatedEventKafkaTemplate;

    @Value("${kafka.topic.create-payment}")
    private String createPaymentTopic;

    public void publishOrderCreated(String orderId, String userId, BigDecimal totalPrice) {
        log.info("Publishing OrderCreatedEvent to topic='{}': orderId={}, userId={}, totalPrice={}",
                createPaymentTopic, orderId, userId, totalPrice);

        com.example.events.OrderCreatedEvent avroEvent = new com.example.events.OrderCreatedEvent();
        avroEvent.setOrderId(orderId);
        avroEvent.setUserId(userId);
        avroEvent.setTotalPrice(totalPrice);

        orderCreatedEventKafkaTemplate
                .send(createPaymentTopic, orderId, avroEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderCreatedEvent for orderId={}: {}",
                                orderId, ex.getMessage(), ex);
                    } else {
                        log.debug("OrderCreatedEvent sent for orderId={}, partition={}, offset={}",
                                orderId,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
