package com.innowise.orderservice.client;

import com.innowise.orderservice.client.dto.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceClient {

    private final UserClient userClient;

    @CircuitBreaker(
            name = "userService",
            fallbackMethod = "userFallback"
    )
    public UserResponse getUser(Long userId) {
        return userClient.getByUserId(userId);
    }

    public UserResponse userFallback(
            Long userId,
            Throwable t
    ) {

        log.warn("Fallback triggered for userId={}", userId, t);

        return new UserResponse(
                userId,
                "unknown",
                "unknown",
                "UNKNOWN",
                false
        );
    }
}
