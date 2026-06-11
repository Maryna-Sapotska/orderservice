package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.dto.OrderFilter;
import com.innowise.orderservice.model.entity.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class OrderSpecification {
    private OrderSpecification() {
    }


    public static Specification<Order> filter(
            OrderFilter filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    cb.isFalse(root.get("deleted"))
            );

            if (filter.getCreatedFrom() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                filter.getCreatedFrom()
                        )
                );
            }

            if (filter.getCreatedTo() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("createdAt"),
                                filter.getCreatedTo()
                        )
                );
            }

            if (filter.getStatuses() != null
                    && !filter.getStatuses().isEmpty()) {

                predicates.add(
                        root.get("status")
                                .in(filter.getStatuses())
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
