package com.innowise.orderservice.controller;

import com.innowise.orderservice.model.dto.OrderFilter;
import com.innowise.orderservice.model.dto.request.CreateOrderRequest;
import com.innowise.orderservice.model.dto.request.UpdateOrderRequest;
import com.innowise.orderservice.model.dto.response.OrderResponse;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(OrderController.REST_URL)
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management API")
public class OrderController {

    public static final String REST_URL = "/orders";

    private final OrderService orderService;

    @Operation(summary = "Create order")
    @ApiResponse(responseCode = "201", description = "Order created")
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(dto));
    }

    @Operation(summary = "Get order by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oder found"),
            @ApiResponse(responseCode = "404", description = "Oder not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @Operation(summary = "Get orders by user id")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getByUserId(@PathVariable Long userId) {

        return ResponseEntity.ok(orderService.getByUserId(userId));
    }

    @Operation(summary = "Get all orders with filters and pagination")
    @ApiResponse(responseCode = "200", description = "Orders retrieved")
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAll(
            @Parameter(description = "Filter by statuses")
            @RequestParam(required = false)
            List<OrderStatus> statuses,

            @Parameter(description = "Filter by created from")
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdFrom,

            @Parameter(description = "Filter by created to")
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdTo,

            @Parameter(description = "Page number (0..N)")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10")
            int size,

            @Parameter(description = "Sorting format: field,direction")
            @RequestParam(defaultValue = "id,asc")
            String sort) {

        String[] sortParams = sort.split(",");

        if (sortParams.length != 2) {
            throw new IllegalArgumentException("Invalid sort format. Use field,direction");
        }

        Sort.Direction direction;

        try {
            direction = Sort.Direction.fromString(sortParams[1]);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Sort direction must be asc or desc"
            );
        }

        OrderFilter filter = new OrderFilter();

        filter.setStatuses(statuses);
        filter.setCreatedFrom(createdFrom);
        filter.setCreatedTo(createdTo);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, sortParams[0])
        );
        return ResponseEntity.ok(orderService.getAll(filter, pageable));
    }

    @Operation(summary = "Update order")
    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateOrderRequest dto) {
        return ResponseEntity.ok(orderService.update(id, dto));
    }

    @Operation(summary = "Delete order")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
