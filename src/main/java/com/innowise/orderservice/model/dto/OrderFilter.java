package com.innowise.orderservice.model.dto;

import com.innowise.orderservice.model.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderFilter {

    private Long userId;

    private LocalDateTime createdFrom;

    private LocalDateTime createdTo;

    private List<OrderStatus> statuses;
}
