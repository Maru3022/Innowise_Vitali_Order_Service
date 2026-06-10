package com.example.innowise_vitali_order_service.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long itemId,
        String itemName,
        BigDecimal itemPrice,
        Integer quantity
) {}