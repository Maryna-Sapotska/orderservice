package com.innowise.orderservice.integration;

import com.innowise.orderservice.model.dto.request.CreateOrderRequest;
import com.innowise.orderservice.model.dto.request.OrderItemRequest;
import com.innowise.orderservice.model.dto.response.OrderResponse;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

class OrderIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    private OrderService orderService;

    @Test
    void shouldCreateOrderWithUserFromFeign() {

        stubUser(1L);

        Item item = new Item();
        item.setName("Laptop");
        item.setPrice(BigDecimal.valueOf(1000));
        item = itemRepository.save(item);

        OrderItemRequest req = new OrderItemRequest();
        req.setItemId(item.getId());
        req.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setItems(List.of(req));

        OrderResponse response = orderService.create(request);

        assertEquals(2000, response.getTotalPrice().intValue());
        assertEquals(1L, response.getUser().getId());
        assertEquals("John", response.getUser().getName());
    }

    @Test
    void shouldReturnFallbackWhenUsersServiceFails() {

        stubUserError();

        Item item = new Item();
        item.setName("Phone");
        item.setPrice(BigDecimal.valueOf(500));
        item = itemRepository.save(item);

        OrderItemRequest req = new OrderItemRequest();
        req.setItemId(item.getId());
        req.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(99L);
        request.setItems(List.of(req));

        OrderResponse response = orderService.create(request);

        assertEquals("unknown", response.getUser().getName());
    }
}
