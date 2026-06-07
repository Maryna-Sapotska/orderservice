package com.innowise.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.controller.OrderController;
import com.innowise.orderservice.model.dto.request.CreateOrderRequest;
import com.innowise.orderservice.model.dto.request.OrderItemRequest;
import com.innowise.orderservice.model.dto.response.OrderResponse;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasSize;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createOrder_shouldReturn201() throws Exception {

        stubUser(1L);

        Item item = new Item();
        item.setName("Laptop");
        item.setPrice(BigDecimal.valueOf(1000));
        item = itemRepository.save(item);

        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setItemId(item.getId());
        itemReq.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setItems(List.of(itemReq));

        mockMvc.perform(post(OrderController.REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(2000))
                .andExpect(jsonPath("$.user.name").value("John"));
    }

    @Test
    void getById_shouldReturnOrder() throws Exception {

        stubUser(1L);

        Item item = new Item();
        item.setName("Phone");
        item.setPrice(BigDecimal.valueOf(500));
        item = itemRepository.save(item);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);

        OrderItemRequest req = new OrderItemRequest();
        req.setItemId(item.getId());
        req.setQuantity(1);

        request.setItems(List.of(req));

        OrderResponse created = objectMapper.readValue(
                mockMvc.perform(post(OrderController.REST_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                OrderResponse.class
        );

        mockMvc.perform(get(OrderController.REST_URL + "/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()));
    }

    @Test
    void getByUserId_shouldReturnOrders() throws Exception {

        stubUser(1L);

        Item item = new Item();
        item.setName("Laptop");
        item.setPrice(BigDecimal.valueOf(1000));
        item = itemRepository.save(item);

        OrderItemRequest req = new OrderItemRequest();
        req.setItemId(item.getId());
        req.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setItems(List.of(req));

        mockMvc.perform(post(OrderController.REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(OrderController.REST_URL + "/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalPrice").value(1000))
                .andExpect(jsonPath("$[0].user.name").value("John"))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].itemName").value("Laptop"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {

        stubUser(1L);

        Item item = new Item();
        item.setName("Mouse");
        item.setPrice(BigDecimal.valueOf(100));
        item = itemRepository.save(item);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);

        OrderItemRequest req = new OrderItemRequest();
        req.setItemId(item.getId());
        req.setQuantity(1);

        request.setItems(List.of(req));

        OrderResponse created = objectMapper.readValue(
                mockMvc.perform(post(OrderController.REST_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                OrderResponse.class
        );

        mockMvc.perform(delete(OrderController.REST_URL + "/" + created.getId()))
                .andExpect(status().isNoContent());
    }
}
