package com.swiftbite.order.dto.request;

import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull(message = "Delivery address is required")
    private String deliveryAddress;

    private String deliveryCity;

    private String notes;

    public CreateOrderRequest() {}

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getDeliveryCity() { return deliveryCity; }
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
