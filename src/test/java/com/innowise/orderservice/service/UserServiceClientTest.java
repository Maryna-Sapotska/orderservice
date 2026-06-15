package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.client.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class UserServiceClientTest {

    @InjectMocks
    private UserServiceClient userServiceClient;

    @Test
    void userFallback_shouldReturnUnknownUser() {

        UserResponse result =
                userServiceClient.userFallback(
                        10L,
                        new RuntimeException("boom")
                );

        assertEquals(10L, result.getId());
        assertEquals("unknown", result.getName());
        assertEquals("unknown", result.getSurname());
        assertEquals("UNKNOWN", result.getEmail());
        assertFalse(result.isActive());
    }
}
