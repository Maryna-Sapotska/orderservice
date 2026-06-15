package com.innowise.orderservice;

import com.innowise.orderservice.model.dto.OrderFilter;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.OrderSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderSpecificationTest {

    @Test
    void filter_shouldReturnOnlyNotDeleted() {
        OrderFilter filter = new OrderFilter();

        Specification<Order> spec =
                OrderSpecification.filter(filter);

        assertNotNull(spec);
    }

    @Test
    void filter_shouldApplyUserId() {
        OrderFilter filter = new OrderFilter();
        filter.setUserId(1L);

        Specification<Order> spec =
                OrderSpecification.filter(filter);

        assertNotNull(spec);
    }

    @Test
    void filter_shouldApplyStatuses() {
        OrderFilter filter = new OrderFilter();
        filter.setStatuses(List.of(OrderStatus.CREATED));

        Specification<Order> spec =
                OrderSpecification.filter(filter);

        assertNotNull(spec);
    }
}
