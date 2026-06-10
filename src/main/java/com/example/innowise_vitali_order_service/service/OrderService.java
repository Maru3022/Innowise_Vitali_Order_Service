package com.example.innowise_vitali_order_service.service;

import com.example.innowise_vitali_order_service.dto.request.CreateOrderRequest;
import com.example.innowise_vitali_order_service.dto.response.OrderResponse;
import com.example.innowise_vitali_order_service.entity.OrderStatus;
import org.hibernate.query.Page;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    Page<OrderResponse> getOrders(
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            List<OrderStatus> statuses,
            Pageable pageable
    );

    List<OrderResponse> getOrdersByUserId(Long id);

    OrderResponse updateOrder(Long id, CreateOrderRequest request);
    void deleteOrder(Long id);
}
