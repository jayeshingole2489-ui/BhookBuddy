package com.swiftbite.user.exception;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(Long addressId) {
        super("Address not found with id: " + addressId);
    }

    public AddressNotFoundException(Long addressId, Long userId) {
        super("Address with id " + addressId + " not found for user " + userId);
    }
}
