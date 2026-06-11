package com.example.innowise_vitali_order_service.service;

import com.example.innowise_vitali_order_service.client.UserServiceClient;
import com.example.innowise_vitali_order_service.dto.request.CreateOrderRequest;
import com.example.innowise_vitali_order_service.dto.request.OrderItemRequest;
import com.example.innowise_vitali_order_service.dto.request.UpdateOrderRequest;
import com.example.innowise_vitali_order_service.dto.response.OrderResponse;
import com.example.innowise_vitali_order_service.dto.response.UserInfo;
import com.example.innowise_vitali_order_service.entity.Item;
import com.example.innowise_vitali_order_service.entity.Order;
import com.example.innowise_vitali_order_service.entity.OrderStatus;
import com.example.innowise_vitali_order_service.exception.OrderNotFoundException;
import com.example.innowise_vitali_order_service.mapper.OrderMapper;
import com.example.innowise_vitali_order_service.repository.ItemRepository;
import com.example.innowise_vitali_order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private OrderMapper orderMapper;
    @InjectMocks private OrderServiceImpl orderService;

    private Item item;
    private Order order;
    private UserInfo userInfo;

    @BeforeEach
    void setUp() {
        item = Item.builder().id(1L).name("Item").price(new BigDecimal("10.00")).build();
        order = Order.builder().id(1L).userId(42L)
                .status(OrderStatus.PENDING).totalPrice(BigDecimal.TEN)
                .deleted(false).orderItems(new ArrayList<>()).build();
        userInfo = new UserInfo(42L, "42@placeholder.com", "John", "Doe");
    }

    @Test
    void createOrder_shouldSaveAndReturnResponse() {
        CreateOrderRequest req = new CreateOrderRequest(42L, List.of(new OrderItemRequest(1L, 2)));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any())).thenReturn(order);
        when(userServiceClient.getUserByEmail("42@placeholder.com")).thenReturn(userInfo);
        OrderResponse mock = new OrderResponse(1L, 42L, OrderStatus.PENDING,
                new BigDecimal("20.00"), List.of(), null, null, userInfo);
        when(orderMapper.toResponseWithUser(any(), any())).thenReturn(mock);

        OrderResponse result = orderService.createOrder(req);

        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userServiceClient.getUserByEmail("42@placeholder.com")).thenReturn(userInfo);
        OrderResponse mock = new OrderResponse(1L, 42L, OrderStatus.PENDING,
                BigDecimal.TEN, List.of(), null, null, userInfo);
        when(orderMapper.toResponseWithUser(any(), any())).thenReturn(mock);

        OrderResponse result = orderService.getOrderById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void getOrderById_shouldThrow_whenNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void updateOrder_shouldUpdateStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(userServiceClient.getUserByEmail("42@placeholder.com")).thenReturn(userInfo);
        OrderResponse mock = new OrderResponse(1L, 42L, OrderStatus.CONFIRMED,
                BigDecimal.TEN, List.of(), null, null, userInfo);
        when(orderMapper.toResponseWithUser(any(), any())).thenReturn(mock);

        OrderResponse result = orderService.updateOrder(1L, new UpdateOrderRequest(OrderStatus.CONFIRMED, null));

        assertThat(result.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void deleteOrder_shouldSoftDelete() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        orderService.deleteOrder(1L);

        assertThat(order.getDeleted()).isTrue();
        verify(orderRepository).save(order);
    }

    @Test
    void deleteOrder_shouldThrow_whenNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.deleteOrder(99L))
                .isInstanceOf(OrderNotFoundException.class);
        verify(orderRepository, never()).save(any());
    }
}