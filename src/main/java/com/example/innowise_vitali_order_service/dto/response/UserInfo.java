package com.example.innowise_vitali_order_service.dto.response;

public record UserInfo(
   Long id,
   String email,
   String firstName,
   String lastName
) {}