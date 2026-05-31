package com.innowise.orderservice.controller;

import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.OrderFilter;
import com.innowise.orderservice.model.dto.request.CreateOrderRequest;
import com.innowise.orderservice.model.dto.request.UpdateOrderRequest;
import com.innowise.orderservice.model.dto.response.OrderResponse;
import com.innowise.orderservice.model.entity.Order;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(OrderController.REST_URL)
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management API")
public class OrderController {

    public static final String REST_URL = "/orders";

    private final OrderService orderService;
    private final OrderMapper mapper;

    @Operation(summary = "Create order")
    @ApiResponse(responseCode = "201", description = "Order created")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest dto) {

        Order order = orderService.create(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(order));
    }

    @Operation(summary = "Get order by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN') or #id.toString() == authentication.principal")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity
                .ok(mapper.toResponse(orderService.getById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getByUserId(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                mapper.toResponse(
                        orderService.getByUserId(userId)
                )
        );
    }

//    @Operation(
//            summary = "Get user with cards",
//            description = "Returns user info together with all user cards"
//    )
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "User with cards retrieved"),
//            @ApiResponse(responseCode = "404", description = "User not found")
//    })
//    @PreAuthorize("hasAnyRole('ADMIN') or #id.toString() == authentication.principal")
//    @GetMapping("/{userId}/cards")
//    public ResponseEntity<UserWithCardsDto> getUserWithCards(
//            @Parameter(description = "User id")
//            @PathVariable Long userId){
//        return ResponseEntity
//                .ok(orderService.getUserWithCards(userId));
//    }

    @Operation(summary = "Get all orders with filters and pagination")
    @ApiResponse(responseCode = "200", description = "Orders retrieved")
    @PreAuthorize("hasRole('ADMIN')")
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

        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);

        OrderFilter filter = new OrderFilter();

        filter.setStatuses(statuses);
        filter.setCreatedFrom(createdFrom);
        filter.setCreatedTo(createdTo);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, sortParams[0])
        );
        return ResponseEntity
                .ok(orderService.getAll(filter, pageable).map(mapper::toResponse));
    }

    @Operation(summary = "Update order")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateOrderRequest dto) {
        return ResponseEntity
                .ok(mapper.toResponse(orderService.update(id, dto)));
    }

    @Operation(summary = "Delete order")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
