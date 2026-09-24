package com.swiftbite.payment.dto;

import jakarta.validation.constraints.NotBlank;

public class RefundRequest {

    @NotBlank(message = "Reason is required")
    private String reason;

    public RefundRequest() {}

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
