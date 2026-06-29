package com.example.innowise_vitali_order_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKafkaProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> orderCreatedEventKafkaTemplate;

    @Value("${kafka.topic.create-payment}")
    private String createPaymentTopic;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent to topic='{}': orderId={}, userId={}, totalPrice={}",
                createPaymentTopic, event.orderId(), event.userId(), event.totalPrice());

        orderCreatedEventKafkaTemplate
                .send(createPaymentTopic, event.orderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderCreatedEvent for orderId={}: {}",
                                event.orderId(), ex.getMessage(), ex);
                    } else {
                        log.debug("OrderCreatedEvent sent for orderId={}, partition={}, offset={}",
                                event.orderId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
