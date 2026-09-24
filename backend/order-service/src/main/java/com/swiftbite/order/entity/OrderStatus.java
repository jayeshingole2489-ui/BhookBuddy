package com.swiftbite.order.entity;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    PAYMENT_FAILED
}
