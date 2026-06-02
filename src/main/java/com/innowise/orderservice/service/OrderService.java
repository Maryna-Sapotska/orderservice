package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.client.dto.UserResponse;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.OrderFilter;
import com.innowise.orderservice.model.dto.request.CreateOrderRequest;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserClient userClient;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {

        Order order = orderMapper.toEntity(request);
        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : order.getOrderItems()) {

            Item dbItem = itemRepository.findById(item.getItem().getId())
                    .orElseThrow();

            item.setItem(dbItem);
            item.setOrder(order);

            total = total.add(
                    dbItem.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }

        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);

        return buildResponse(saved);
    }

    public OrderResponse getById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        return buildResponse(order);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "userFallback")
    public List<OrderResponse> getByUserId(Long userId) {

        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    public OrderResponse userFallback(Long id, Exception ex) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        OrderResponse response = orderMapper.toResponse(order);

        response.setUser(new UserResponse(
                null, "unknown", "unknown", "UNKNOWN", false
        ));

        return response;
    }

    public Page<OrderResponse> getAll(OrderFilter filter, Pageable pageable) {

        return orderRepository.findAll(OrderSpecification.filter(filter), pageable)
                .map(this::buildResponse);
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

        UserResponse user = userClient.getByUserId(order.getUserId());
        response.setUser(user);

        return response;
    }
}
