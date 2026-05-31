package com.innowise.orderservice.service;

import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.model.dto.OrderFilter;
import com.innowise.orderservice.model.dto.request.CreateOrderRequest;
import com.innowise.orderservice.model.dto.request.OrderItemRequest;
import com.innowise.orderservice.model.dto.request.UpdateOrderRequest;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.repository.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    public Order create(CreateOrderRequest request) {

        Order order = new Order();

        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.CREATED);

        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest dto : request.getItems()) {

            Item item = itemRepository.findById(dto.getItemId())
                    .orElseThrow();

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setQuantity(dto.getQuantity());

            total = total.add(
                    item.getPrice()
                            .multiply(BigDecimal.valueOf(dto.getQuantity()))
            );

            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(total);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Order> getByUserId(Long userId) {

        return orderRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<Order> getAll(
            OrderFilter filter,
            Pageable pageable
    ) {

        return orderRepository.findAll(
                OrderSpecification.filter(filter),
                pageable
        );
    }

    public Order update(
            Long id,
            UpdateOrderRequest request
    ) {

        Order order = getById(id);

        order.setStatus(request.getStatus());

        return orderRepository.save(order);
    }

    public void delete(Long id) {

        orderRepository.delete(getById(id));
    }
}
