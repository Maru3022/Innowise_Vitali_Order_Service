package com.example.innowise_vitali_order_service.repository;

import com.example.innowise_vitali_order_service.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByName(String name);
}