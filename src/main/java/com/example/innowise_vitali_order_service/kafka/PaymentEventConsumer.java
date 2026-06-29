package com.example.innowise_vitali_order_service.kafka;

import com.example.innowise_vitali_order_service.entity.Order;
import com.example.innowise_vitali_order_service.entity.OrderStatus;
import com.example.innowise_vitali_order_service.exception.OrderNotFoundException;
import com.example.innowise_vitali_order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "${kafka.topic.payment}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(
            @Payload PaymentEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received PaymentEvent from topic='{}' offset={}: paymentId={}, orderId={}, status={}",
                topic, offset, event.getPaymentId(), event.getOrderId(), event.getStatus());

        OrderStatus newStatus = resolveOrderStatus(event.getStatus());
        if (newStatus == null) {
            log.warn("Unknown payment status '{}' for paymentId={}, orderId={} — skipping",
                    event.getStatus(), event.getPaymentId(), event.getOrderId());
            return;
        }

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> {
                    log.error("Order not found for orderId={} from paymentId={}",
                            event.getOrderId(), event.getPaymentId());
                    return new OrderNotFoundException(event.getOrderId());
                });

        order.setStatus(newStatus);
        orderRepository.save(order);

        log.info("Order id={} status updated to {} based on paymentId={}",
                order.getId(), newStatus, event.getPaymentId());
    }

    /**
     * Maps payment status string to OrderStatus.
     * Returns null for unknown statuses so the message is skipped without retrying.
     */
    private OrderStatus resolveOrderStatus(String paymentStatus) {
        if (paymentStatus == null) {
            return null;
        }
        return switch (paymentStatus.toUpperCase()) {
            case "SUCCESS" -> OrderStatus.PAID;
            case "FAILED"  -> OrderStatus.CANCELLED;
            default        -> null;
        };
    }
}
