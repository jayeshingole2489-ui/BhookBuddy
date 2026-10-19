package com.swiftbite.delivery.controller;

import com.swiftbite.delivery.dto.DeliveryResponse;
import com.swiftbite.delivery.service.DeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @PathVariable String orderId) {
        DeliveryResponse response = deliveryService.getDeliveryByOrderId(orderId);
        boolean isAdmin = roles != null && (roles.contains("ROLE_ADMIN") || roles.contains("ADMIN"));
        if (!isAdmin && userId != null && !response.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @PathVariable String deliveryId) {
        DeliveryResponse response = deliveryService.getDeliveryById(deliveryId);
        boolean isAdmin = roles != null && (roles.contains("ROLE_ADMIN") || roles.contains("ADMIN"));
        if (!isAdmin && userId != null && !response.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(response);
    }
}
