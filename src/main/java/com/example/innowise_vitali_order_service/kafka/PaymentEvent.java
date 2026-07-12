package com.example.innowise_vitali_order_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private String paymentId;
    private String orderId;
    private String userId;
    private PaymentStatus status;
}
