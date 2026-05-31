package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Item;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository {

    List<Item> findById(); // переделать
}
