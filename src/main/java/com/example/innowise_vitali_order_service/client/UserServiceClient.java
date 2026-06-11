package com.example.innowise_vitali_order_service.client;

import com.example.innowise_vitali_order_service.dto.response.UserInfo;
import com.example.innowise_vitali_order_service.exception.UserServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service",
        url = "${spring.cloud.openfeign.client.config.user-service.url}")
public interface UserServiceClient {

    @GetMapping("/api/users/by-email")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserByEmailFallback")
    UserInfo getUserByEmail(@RequestParam("email") String email);

    default UserInfo getUserByEmailFallback(String email, Throwable ex){
        throw new UserServiceUnavailableException(
                "User Service unavailable for email: " + email, ex
        );
    }
}