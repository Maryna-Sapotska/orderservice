package com.innowise.orderservice.model.dto.request;

import com.innowise.orderservice.model.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderRequest {

    private OrderStatus orderStatus;
}
