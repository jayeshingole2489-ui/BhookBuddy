package com.swiftbite.payment.service;

import com.swiftbite.payment.dto.PaymentRequest;
import com.swiftbite.payment.dto.PaymentResponse;
import com.swiftbite.payment.dto.RefundRequest;
import com.swiftbite.payment.entity.Payment;
import com.swiftbite.payment.entity.PaymentStatus;
import com.swiftbite.payment.kafka.PaymentEventProducer;
import com.swiftbite.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId("order-123");
        paymentRequest.setAmount(new BigDecimal("250.00"));
        paymentRequest.setPaymentMethod("CARD");
        paymentRequest.setSimulateFailure(false);
    }

    @Test
    void initiatePayment_success() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse response = paymentService.initiatePayment(1L, paymentRequest);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertNotNull(response.getTransactionId());
        assertTrue(response.getTransactionId().startsWith("TXN"));
        verify(paymentEventProducer, times(1)).publishPaymentEvent(any(), any());
    }

    @Test
    void initiatePayment_failure_simulateFailure() {
        paymentRequest.setSimulateFailure(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse response = paymentService.initiatePayment(1L, paymentRequest);

        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertEquals("Insufficient funds", response.getFailureReason());
        verify(paymentEventProducer, times(1)).publishPaymentEvent(any(), any());
    }

    @Test
    void processRefund_invalidState_throwsException() {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.FAILED);

        when(paymentRepository.findByPaymentId(any())).thenReturn(Optional.of(payment));

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setReason("Customer request");

        assertThrows(IllegalStateException.class, () ->
                paymentService.processRefund(payment.getPaymentId(), refundRequest));
    }

    @Test
    void processRefund_success() {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setOrderId("order-123");
        payment.setUserId(1L);
        payment.setAmount(new BigDecimal("250.00"));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentMethod("CARD");

        when(paymentRepository.findByPaymentId(any())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setReason("Customer request");

        PaymentResponse response = paymentService.processRefund(payment.getPaymentId(), refundRequest);

        assertEquals(PaymentStatus.REFUNDED, response.getStatus());
        verify(paymentEventProducer, times(1)).publishPaymentEvent(any(), any());
    }
}
