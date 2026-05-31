package com.innowise.orderservice.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotEmpty(message = "Order must contain items")
    private List<@Valid OrderItemRequest> items;
}
