package com.swiftbite.delivery.controller;

import com.swiftbite.delivery.dto.DeliveryResponse;
import com.swiftbite.delivery.dto.UpdateDeliveryStatusRequest;
import com.swiftbite.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/deliveries")
public class AdminDeliveryController {

    private final DeliveryService deliveryService;

    public AdminDeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @PutMapping("/{deliveryId}/status")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable String deliveryId,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        try {
            DeliveryResponse response = deliveryService.updateDeliveryStatus(deliveryId, request.getStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
