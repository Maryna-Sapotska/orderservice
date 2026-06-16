package com.innowise.orderservice.model.dto.request;

import com.innowise.orderservice.model.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus orderStatus;
}
