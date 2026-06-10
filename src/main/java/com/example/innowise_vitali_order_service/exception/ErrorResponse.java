package com.example.innowise_vitali_order_service.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String,String> fieldErrors
) {}