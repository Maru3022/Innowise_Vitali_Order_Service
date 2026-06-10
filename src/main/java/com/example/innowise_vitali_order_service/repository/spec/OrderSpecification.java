package com.example.innowise_vitali_order_service.repository.spec;

import com.example.innowise_vitali_order_service.entity.Order;
import com.example.innowise_vitali_order_service.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    private OrderSpecification() {}

    public static Specification<Order> withFilters(
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            List<OrderStatus> statuses,
            Long userId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }
            if (createdTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}