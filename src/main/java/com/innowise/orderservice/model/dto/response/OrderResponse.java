package com.innowise.orderservice.model.dto.response;

import com.innowise.orderservice.model.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrderResponse {

    private Long id;

    private Long userId;

    private OrderStatus status;

    private BigDecimal totalPrice;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;
}
