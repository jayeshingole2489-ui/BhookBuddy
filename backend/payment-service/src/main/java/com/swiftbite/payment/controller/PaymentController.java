package com.swiftbite.payment.controller;

import com.swiftbite.payment.dto.PaymentRequest;
import com.swiftbite.payment.dto.PaymentResponse;
import com.swiftbite.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.initiatePayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String paymentId) {
        PaymentResponse response = paymentService.getPaymentById(paymentId);
        if (!response.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        if (!response.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(response);
    }
}
