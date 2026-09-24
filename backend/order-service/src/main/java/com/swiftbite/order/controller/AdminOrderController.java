package com.swiftbite.order.controller;

import com.swiftbite.order.dto.request.UpdateOrderStatusRequest;
import com.swiftbite.order.dto.response.ApiResponse;
import com.swiftbite.order.dto.response.OrderItemResponse;
import com.swiftbite.order.dto.response.OrderResponse;
import com.swiftbite.order.entity.Order;
import com.swiftbite.order.exception.OrderNotFoundException;
import com.swiftbite.order.repository.OrderRepository;
import com.swiftbite.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Orders", description = "Administrative order management")
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public AdminOrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    @Operation(summary = "Get all orders (admin)")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get any order by business order ID (admin)")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        return ResponseEntity.ok(ApiResponse.success(toOrderResponse(order)));
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status (admin)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse order = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }

    private OrderResponse toOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderId(order.getOrderId());
        response.setRestaurantId(order.getRestaurantId());
        response.setRestaurantName(order.getRestaurantName());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setSubtotal(order.getSubtotal());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setTotal(order.getTotal());
        response.setStatus(order.getStatus());
        response.setPaymentId(order.getPaymentId());
        response.setDeliveryId(order.getDeliveryId());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getMenuItemId(),
                        item.getName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());
        response.setItems(items);
        return response;
    }
}
