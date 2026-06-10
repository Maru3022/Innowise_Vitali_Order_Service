package com.example.innowise_vitali_order_service.dto.response;

import com.example.innowise_vitali_order_service.dto.request.OrderItemRequest;
import com.example.innowise_vitali_order_service.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userid,
        OrderStatus status,
        BigDecimal totalPrice,
        List<OrderItemRequest> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UserInfo user
) {}