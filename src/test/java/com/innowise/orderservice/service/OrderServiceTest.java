package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.client.dto.UserResponse;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.OrderFilter;
import com.innowise.orderservice.model.dto.request.CreateOrderRequest;
import com.innowise.orderservice.model.dto.request.OrderItemRequest;
import com.innowise.orderservice.model.dto.request.UpdateOrderRequest;
import com.innowise.orderservice.model.dto.response.OrderResponse;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void create_shouldCreateOrderAndReturnResponse() {
        Long userId = 1L;

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(userId);

        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setItemId(10L);
        itemReq.setQuantity(2);

        request.setItems(List.of(itemReq));

        Item item = new Item();
        item.setId(10L);
        item.setPrice(BigDecimal.valueOf(100));

        Order savedOrder = new Order();
        savedOrder.setId(100L);
        savedOrder.setUserId(userId);
        savedOrder.setTotalPrice(BigDecimal.valueOf(200));
        savedOrder.setOrderItems(new ArrayList<>());

        OrderResponse response = new OrderResponse();
        response.setTotalPrice(BigDecimal.valueOf(200));
        response.setUser(null);
        response.setId(100L);

        UserResponse user = new UserResponse(
                userId,
                "john",
                "john@test.com",
                "USER",
                true
        );

        Order mappedOrder = new Order();
        mappedOrder.setUserId(userId);

        when(orderMapper.toEntity(any(CreateOrderRequest.class)))
                .thenReturn(mappedOrder);

        when(itemRepository.findById(10L))
                .thenReturn(Optional.of(item));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(response);

        when(userClient.getByUserId(userId))
                .thenReturn(user);

        OrderResponse result = orderService.create(request);

        assertNotNull(result);
        assertEquals(userId, result.getUser().getId());
        assertEquals("john", result.getUser().getName());
        assertEquals(BigDecimal.valueOf(200), result.getTotalPrice());

        verify(itemRepository).findById(10L);
        verify(orderRepository).save(any(Order.class));
        verify(userClient).getByUserId(userId);
    }

    @Test
    void getById_shouldThrowOrderNotFoundException() {

        when(orderRepository.findByIdWithItems(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getById(1L)
        );
    }

    @Test
    void getById_shouldReturnOrder() {

        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);

        OrderResponse response = OrderResponse.builder().build();

        UserResponse user = new UserResponse(
                10L,
                "john",
                "john@test.com",
                "USER",
                true
        );

        when(orderRepository.findByIdWithItems(1L))
                .thenReturn(Optional.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        when(userClient.getByUserId(10L))
                .thenReturn(user);

        OrderResponse result = orderService.getById(1L);

        assertNotNull(result);
        assertEquals(user, result.getUser());

        verify(orderRepository).findByIdWithItems(1L);
        verify(userClient).getByUserId(10L);
    }

    @Test
    void getByUserId_shouldReturnOrders() {

        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);

        OrderResponse response =
                OrderResponse.builder().build();

        UserResponse user =
                new UserResponse(
                        10L,
                        "john",
                        "john@test.com",
                        "USER",
                        true
                );

        when(orderRepository.findByUserId(10L))
                .thenReturn(List.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        when(userClient.getByUserId(10L))
                .thenReturn(user);

        List<OrderResponse> result =
                orderService.getByUserId(10L);

        assertEquals(1, result.size());

        assertEquals(
                user,
                result.getFirst().getUser()
        );
    }

    @Test
    void getAll_shouldReturnPageOfOrders() {

        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);

        Page<Order> page =
                new PageImpl<>(List.of(order));

        OrderResponse response =
                OrderResponse.builder().build();

        UserResponse user =
                new UserResponse(
                        10L,
                        "john",
                        "john@test.com",
                        "USER",
                        true
                );

        when(orderRepository.findAll(
                any(Specification.class),
                any(Pageable.class)))
                .thenReturn(page);

        when(orderRepository.findAllWithItemsByIds(
                List.of(1L)))
                .thenReturn(List.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        when(userClient.getByUserId(10L))
                .thenReturn(user);

        Page<OrderResponse> result =
                orderService.getAll(
                        new OrderFilter(),
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(user,
                result.getContent().getFirst().getUser());
    }

    @Test
    void update_shouldUpdateStatus() {

        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setStatus(OrderStatus.CREATED);

        UpdateOrderRequest request =
                new UpdateOrderRequest();

        request.setOrderStatus(OrderStatus.PAID);

        OrderResponse response =
                OrderResponse.builder().build();

        UserResponse user =
                new UserResponse(
                        10L,
                        "john",
                        "john@test.com",
                        "USER",
                        true
                );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        when(userClient.getByUserId(10L))
                .thenReturn(user);

        OrderResponse result =
                orderService.update(1L, request);

        assertEquals(
                OrderStatus.PAID,
                order.getStatus()
        );

        assertEquals(
                user,
                result.getUser()
        );
    }

    @Test
    void update_shouldThrowWhenOrderNotFound() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        UpdateOrderRequest request =
                new UpdateOrderRequest();

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.update(1L, request)
        );
    }

    @Test
    void delete_shouldDeleteOrder() {

        Long orderId = 1L;

        orderService.delete(orderId);

        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void userFallback_shouldReturnUnknownUser() {

        UserResponse result =
                orderService.userFallback(
                        10L,
                        new RuntimeException("boom")
                );

        assertEquals(10L, result.getId());
        assertEquals("unknown", result.getName());
        assertEquals("unknown", result.getSurname());
        assertEquals("UNKNOWN", result.getEmail());
        assertFalse(result.isActive());
    }
}