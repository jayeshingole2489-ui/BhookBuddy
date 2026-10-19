package com.swiftbite.delivery.dto;

import jakarta.validation.constraints.NotBlank;

public class AssignDeliveryRequest {

    @NotBlank(message = "Delivery person name is required")
    private String deliveryPersonName;

    @NotBlank(message = "Delivery person phone is required")
    private String deliveryPersonPhone;

    public AssignDeliveryRequest() {}

    public AssignDeliveryRequest(String deliveryPersonName, String deliveryPersonPhone) {
        this.deliveryPersonName = deliveryPersonName;
        this.deliveryPersonPhone = deliveryPersonPhone;
    }

    public String getDeliveryPersonName() { return deliveryPersonName; }
    public void setDeliveryPersonName(String deliveryPersonName) { this.deliveryPersonName = deliveryPersonName; }

    public String getDeliveryPersonPhone() { return deliveryPersonPhone; }
    public void setDeliveryPersonPhone(String deliveryPersonPhone) { this.deliveryPersonPhone = deliveryPersonPhone; }
}
