package com.innowise.orderservice.client;

import com.innowise.orderservice.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users-service",
        url = "${services.users-service.url}")
public interface UserClient {

    @GetMapping("/users/{userId}")
    UserResponse getByUserId(@PathVariable Long userId);
}
