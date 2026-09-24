package com.swiftbite.order.exception;

public class RestaurantServiceException extends RuntimeException {

    public RestaurantServiceException(String message) {
        super(message);
    }

    public RestaurantServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
