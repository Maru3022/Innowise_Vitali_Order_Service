package com.example.innowise_vitali_order_service.kafka;

import com.example.innowise_vitali_order_service.entity.Order;
import com.example.innowise_vitali_order_service.entity.OrderStatus;
import com.example.innowise_vitali_order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;
    private final AvroEventMapper avroEventMapper;

    @KafkaListener(
            topics = "${kafka.topic.payment-result}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(com.example.events.PaymentEvent event) {
        PaymentEvent paymentEvent = avroEventMapper.toPaymentDomain(event);

        log.info("Received PaymentEvent: paymentId={}, orderId={}, status={}",
                paymentEvent.getPaymentId(), paymentEvent.getOrderId(), paymentEvent.getStatus());

        OrderStatus newStatus = switch (paymentEvent.getStatus()) {
            case SUCCESS -> OrderStatus.PAID;
            case FAILED, REJECTED -> OrderStatus.CANCELLED;
            default -> null;
        };

        if (newStatus == null) {
            log.warn("Ignoring PaymentEvent with status={} for orderId={}",
                    paymentEvent.getStatus(), paymentEvent.getOrderId());
            return;
        }

        Long orderId;
        try {
            orderId = Long.parseLong(paymentEvent.getOrderId());
        } catch (NumberFormatException e) {
            log.error("Invalid orderId format '{}' in paymentId={}",
                    paymentEvent.getOrderId(), paymentEvent.getPaymentId());
            return;
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.error("Order not found: orderId={}, paymentId={}", orderId, paymentEvent.getPaymentId());
            return;
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        log.info("Order {} status updated to {} (paymentId={})",
                order.getId(), newStatus, paymentEvent.getPaymentId());
    }
}
