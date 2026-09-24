package com.swiftbite.delivery.dto;

import com.swiftbite.delivery.entity.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateDeliveryStatusRequest {

    @NotNull(message = "Status is required")
    private DeliveryStatus status;

    public UpdateDeliveryStatusRequest() {}

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
}
