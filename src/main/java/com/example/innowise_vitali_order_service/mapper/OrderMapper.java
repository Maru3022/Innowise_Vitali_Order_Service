package com.example.innowise_vitali_order_service.mapper;

import com.example.innowise_vitali_order_service.dto.response.OrderItemResponse;
import com.example.innowise_vitali_order_service.dto.response.OrderResponse;
import com.example.innowise_vitali_order_service.dto.response.UserInfo;
import com.example.innowise_vitali_order_service.entity.Order;
import com.example.innowise_vitali_order_service.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "items", source = "orderItems")
    OrderResponse toResponse(Order order);

    default OrderResponse toResponseWithUser(Order order, UserInfo userInfo) {
        OrderResponse base = toResponse(order);
        return new OrderResponse(
                base.id(),
                base.userId(),
                base.status(),
                base.totalPrice(),
                base.items(),
                base.createdAt(),
                base.updatedAt(),
                userInfo
        );
    }

    List<OrderResponse> toResponseList(List<Order> orders);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemPrice", source = "item.price")
    OrderItemResponse toItemResponse(OrderItem orderItem);
}