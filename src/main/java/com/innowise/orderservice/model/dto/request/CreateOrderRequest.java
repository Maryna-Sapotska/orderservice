package com.innowise.orderservice.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    @NotBlank(message = "User id is required")
    private String userId;

    @NotEmpty(message = "Order must contain items")
    private List<@Valid OrderItemRequest> items;
}
