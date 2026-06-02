package com.innowise.orderservice.client;

import com.innowise.orderservice.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/userId/{userId}")
    UserResponse getByUserId(@PathVariable Long userId);
}
