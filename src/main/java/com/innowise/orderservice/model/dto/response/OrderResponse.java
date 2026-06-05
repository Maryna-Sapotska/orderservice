package com.innowise.orderservice.model.dto.response;

import com.innowise.orderservice.client.dto.UserResponse;
import com.innowise.orderservice.model.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private UserResponse user;
}
