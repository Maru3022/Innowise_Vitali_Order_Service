package com.example.innowise_vitali_order_service.entity;

import jakarta.persistence.Access;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "items")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
}
