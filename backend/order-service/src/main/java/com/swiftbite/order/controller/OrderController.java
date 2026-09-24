package com.swiftbite.order.controller;

import com.swiftbite.order.dto.request.CreateOrderRequest;
import com.swiftbite.order.dto.response.ApiResponse;
import com.swiftbite.order.dto.response.OrderResponse;
import com.swiftbite.order.security.AuthenticatedUser;
import com.swiftbite.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management for customers")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create order from cart")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(user.getUserId(), user.getEmail(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    @GetMapping
    @Operation(summary = "Get order history for current user")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal AuthenticatedUser user) {
        List<OrderResponse> orders = orderService.getOrdersByUser(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get a specific order by business order ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String orderId) {
        OrderResponse order = orderService.getOrderById(orderId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String orderId) {
        OrderResponse order = orderService.cancelOrder(orderId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", order));
    }
}
