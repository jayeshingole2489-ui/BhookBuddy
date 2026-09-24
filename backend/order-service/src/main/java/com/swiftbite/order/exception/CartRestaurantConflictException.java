package com.swiftbite.order.exception;

public class CartRestaurantConflictException extends RuntimeException {

    public CartRestaurantConflictException(String message) {
        super(message);
    }
}
