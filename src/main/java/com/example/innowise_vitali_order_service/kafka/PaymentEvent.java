package com.example.innowise_vitali_order_service.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a payment event consumed from the CREATE_PAYMENT Kafka topic.
 * Mirrors the PaymentEvent produced by the Payment Service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentEvent {

    private String paymentId;
    private Long orderId;
    private String status;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
