package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.exception.ItemNotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {

        Order order = orderMapper.toEntity(request);
        order.setStatus(OrderStatus.CREATED);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest req : request.getItems()) {

            Item dbItem = itemRepository.findById(req.getItemId())
                    .orElseThrow(()-> new ItemNotFoundException(req.getItemId()));

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
                .map(o -> buildResponse(orderMap.get(o.getId())))
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
            response.setUser(userServiceClient.getUser(order.getUserId()));

        return response;
    }
}
