package com.example.innowise_vitali_order_service.service;

import com.example.innowise_vitali_order_service.client.UserServiceClient;
import com.example.innowise_vitali_order_service.dto.request.CreateOrderRequest;
import com.example.innowise_vitali_order_service.dto.request.OrderItemRequest;
import com.example.innowise_vitali_order_service.dto.request.UpdateOrderRequest;
import com.example.innowise_vitali_order_service.dto.response.OrderResponse;
import com.example.innowise_vitali_order_service.dto.response.UserInfo;
import com.example.innowise_vitali_order_service.entity.Item;
import com.example.innowise_vitali_order_service.entity.Order;
import com.example.innowise_vitali_order_service.entity.OrderItem;
import com.example.innowise_vitali_order_service.entity.OrderStatus;
import com.example.innowise_vitali_order_service.exception.ItemNotFoundException;
import com.example.innowise_vitali_order_service.exception.OrderNotFoundException;
import com.example.innowise_vitali_order_service.mapper.OrderMapper;
import com.example.innowise_vitali_order_service.repository.ItemRepository;
import com.example.innowise_vitali_order_service.repository.OrderRepository;
import com.example.innowise_vitali_order_service.repository.spec.OrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserServiceClient userServiceClient;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .userId(request.userId())
                .status(OrderStatus.PENDING)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : request.items()) {
            Item item = itemRepository.findById(itemReq.itemId())
                    .orElseThrow(() -> new ItemNotFoundException(itemReq.itemId()));
            OrderItem orderItem = OrderItem.builder()
                    .item(item)
                    .quantity(itemReq.quantity())
                    .build();
            order.addOrderItem(orderItem);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity())));
        }
        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);
        return orderMapper.toResponseWithUser(saved, fetchUserInfo(saved.getUserId()));
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = findById(id);
        return orderMapper.toResponseWithUser(order, fetchUserInfo(order.getUserId()));
    }

    @Override
    public Page<OrderResponse> getOrders(LocalDateTime createdFrom, LocalDateTime createdTo, List<OrderStatus> statuses, Pageable pageable) {
        Specification<Order> spec = OrderSpecification.withFilters(createdFrom, createdTo, statuses, null);
        return orderRepository.findAll(spec, pageable)
                .map(order -> orderMapper.toResponseWithUser(order, fetchUserInfo(order.getUserId())));
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(order -> orderMapper.toResponseWithUser(order, fetchUserInfo(order.getUserId())))
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, UpdateOrderRequest request) {
        Order order = findById(id);
        if (request.status() != null) {
            order.setStatus(request.status());
        }
        if (request.items() != null && !request.items().isEmpty()) {
            order.getOrderItems().clear();
            BigDecimal total = BigDecimal.ZERO;
            for (OrderItemRequest itemReq : request.items()) {
                Item item = itemRepository.findById(itemReq.itemId())
                        .orElseThrow(() -> new ItemNotFoundException(itemReq.itemId()));
                OrderItem orderItem = OrderItem.builder()
                        .item(item).quantity(itemReq.quantity()).build();
                order.addOrderItem(orderItem);
                total = total.add(item.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity())));
            }
            order.setTotalPrice(total);
        }

        Order saved = orderRepository.save(order);
        return orderMapper.toResponseWithUser(saved, fetchUserInfo(saved.getUserId()));
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = findById(id);
        order.setDeleted(true);
        orderRepository.save(order);
    }

    private Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private UserInfo fetchUserInfo(Long userId) {
        try {
            return userServiceClient.getUserByEmail(userId + "@placeholder.com");
        } catch (Exception e) {
            log.warn("Could not fetch user info for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }
}