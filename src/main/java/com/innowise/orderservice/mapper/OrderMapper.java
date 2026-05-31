package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.response.OrderItemResponse;
import com.innowise.orderservice.model.dto.response.OrderResponse;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "items", source = "orderItems")
    OrderResponse toResponse(Order order);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemPrice", source = "item.price")
    OrderItemResponse toResponse(OrderItem orderItem);

    List<OrderResponse> toResponse(List<Order> orders);
}
