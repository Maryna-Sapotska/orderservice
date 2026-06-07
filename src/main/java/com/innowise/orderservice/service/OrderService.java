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
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.repository.OrderSpecification;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserClient userClient;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.CREATED);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest req : request.getItems()) {

            Item dbItem = itemRepository.findById(req.getItemId())
                    .orElseThrow(()-> new RuntimeException("ITEM NOT FOUND: " + req.getItemId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(dbItem);
            orderItem.setQuantity(req.getQuantity());
            orderItem.setOrder(order);

            orderItems.add(orderItem);

            total = total.add(
                    dbItem.getPrice()
                            .multiply(BigDecimal.valueOf(req.getQuantity()))
            );
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);

        return buildResponse(saved);
    }

    public OrderResponse getById(Long id) {

        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        return buildResponse(order);
    }

    public List<OrderResponse> getByUserId(Long userId) {

        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "userFallback")
    protected UserResponse getUser(Long userId) {
        return userClient.getByUserId(userId);
    }

    protected UserResponse userFallback() {

        return new UserResponse(
                null,
                "unknown",
                "unknown",
                "UNKNOWN",
                false
        );
    }

    public Page<OrderResponse> getAll(OrderFilter filter, Pageable pageable) {

        Page<Order> page = orderRepository.findAll(
                OrderSpecification.filter(filter),
                pageable
        );

        List<Long> ids = page.getContent()
                .stream()
                .map(Order::getId)
                .toList();

        List<Order> orders = orderRepository.findAllWithItemsByIds(ids);

        Map<Long, Order> orderMap = orders.stream()
                .collect(Collectors.toMap(Order::getId, o -> o));

        List<OrderResponse> content = page.getContent().stream()
                .map(o -> {
                    Order fullOrder = orderMap.get(o.getId());

                    try {
                        return buildResponse(fullOrder);
                    } catch (Exception e) {
                        OrderResponse response = orderMapper.toResponse(fullOrder);
                        response.setUser(userFallback());
                        return response;
                    }
                })
                .toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional
    public OrderResponse update(Long id, UpdateOrderRequest request) {

        Order order = orderRepository.findById(id)
                .orElseThrow(()-> new OrderNotFoundException(id));

        order.setStatus(request.getOrderStatus());

        Order saved = orderRepository.save(order);

        return buildResponse(saved);
    }

    @Transactional
    public void delete(Long id) {

        orderRepository.deleteById(id);
    }

    private OrderResponse buildResponse(Order order) {

        OrderResponse response = orderMapper.toResponse(order);

        try {
            response.setUser(getUser(order.getUserId()));
        } catch (Exception e) {
            response.setUser(userFallback());
        }

        return response;
    }
}
