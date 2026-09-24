package com.swiftbite.payment.service;

import com.swiftbite.payment.dto.*;
import com.swiftbite.payment.entity.Payment;
import com.swiftbite.payment.entity.PaymentStatus;
import com.swiftbite.payment.kafka.PaymentEventProducer;
import com.swiftbite.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentService(PaymentRepository paymentRepository, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @Transactional
    public PaymentResponse initiatePayment(Long userId, PaymentRequest request) {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setOrderId(request.getOrderId());
        payment.setUserId(userId);
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(request.getPaymentMethod());

        if (!request.isSimulateFailure()) {
            String txnId = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            payment.setTransactionId(txnId);
            payment.setStatus(PaymentStatus.SUCCESS);
            payment = paymentRepository.save(payment);
            logger.info("Payment successful for order {}: txn {}", request.getOrderId(), txnId);
            publishEvent(payment, "PAYMENT_COMPLETED", null);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Insufficient funds");
            payment = paymentRepository.save(payment);
            logger.warn("Payment failed for order {}: Insufficient funds", request.getOrderId());
            publishEvent(payment, "PAYMENT_FAILED", "Insufficient funds");
        }

        return toResponse(payment);
    }

    public PaymentResponse getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        return toResponse(payment);
    }

    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        return toResponse(payment);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse processRefund(String paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only successful payments can be refunded. Current status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);
        logger.info("Payment refunded: {} reason: {}", paymentId, request.getReason());
        publishEvent(payment, "PAYMENT_REFUNDED", null);
        return toResponse(payment);
    }

    private void publishEvent(Payment payment, String eventType, String failureReason) {
        PaymentEvent event = new PaymentEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setOrderId(payment.getOrderId());
        event.setPaymentId(payment.getPaymentId());
        event.setAmount(payment.getAmount());
        event.setFailureReason(failureReason);
        event.setTimestamp(LocalDateTime.now());
        paymentEventProducer.publishPaymentEvent(payment.getOrderId(), event);
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setOrderId(payment.getOrderId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTransactionId(payment.getTransactionId());
        response.setFailureReason(payment.getFailureReason());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}
