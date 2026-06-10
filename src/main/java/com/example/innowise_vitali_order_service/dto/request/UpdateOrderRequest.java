package com.example.innowise_vitali_order_service.dto.request;

import com.example.innowise_vitali_order_service.entity.OrderStatus;
import jakarta.validation.Valid;

import java.util.List;

public record UpdateOrderRequest(
   OrderStatus status,

   @Valid
   List<OrderItemRequest> items
) {}