package com.example.innowise_vitali_order_service.service;

import com.example.innowise_vitali_order_service.client.UserServiceClient;
import com.example.innowise_vitali_order_service.dto.request.UpdateOrderRequest;
import com.example.innowise_vitali_order_service.dto.response.OrderResponse;
import com.example.innowise_vitali_order_service.dto.response.UserInfo;
import com.example.innowise_vitali_order_service.entity.*;
import com.example.innowise_vitali_order_service.kafka.OrderKafkaProducer;
import com.example.innowise_vitali_order_service.mapper.OrderMapper;
import com.example.innowise_vitali_order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderKafkaProducer orderKafkaProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private UserInfo userInfo;

    @BeforeEach
    void setup() {
        order = new Order();
        order.setId(1L);
        order.setUserId(42L);
        order.setStatus(OrderStatus.PENDING);

        userInfo = new UserInfo(42L, "Test", "User", "test@test.com");
    }

    @Test
    void updateOrder_shouldUpdateStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userServiceClient.getUserById(42L)).thenReturn(userInfo);

        OrderResponse mock = new OrderResponse(1L, 42L, OrderStatus.PAID,
                BigDecimal.TEN, List.of(), null, null, userInfo);
        when(orderMapper.toResponseWithUser(any(Order.class), any(UserInfo.class))).thenReturn(mock);

        OrderResponse result = orderService.updateOrder(1L, new UpdateOrderRequest(OrderStatus.PAID, null));

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(OrderStatus.PAID);

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(userServiceClient, times(1)).getUserById(42L);
    }
}