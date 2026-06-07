package com.innowise.orderservice.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemResponse {

    private Long itemId;
    private String itemName;
    private BigDecimal itemPrice;
    private Integer quantity;
}
