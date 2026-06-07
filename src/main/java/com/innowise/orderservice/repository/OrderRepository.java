package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
    List<Order> findByUserId(Long userId);

    @Query("""
select o from Order o
left join fetch o.orderItems oi
left join fetch oi.item
where o.id = :id
""")
    Optional<Order> findByIdWithItems(Long id);

    @Query("""
select distinct o from Order o
left join fetch o.orderItems oi
left join fetch oi.item
where o.id in :ids
""")
    List<Order> findAllWithItemsByIds(List<Long> ids);
}
