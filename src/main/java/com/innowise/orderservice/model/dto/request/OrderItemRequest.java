package com.innowise.orderservice.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {

    @NotNull(message = "Item id is required")
    private Long itemId;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}
