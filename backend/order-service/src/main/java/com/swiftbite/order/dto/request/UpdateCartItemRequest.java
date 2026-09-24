package com.swiftbite.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateCartItemRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be 0 or more (0 removes the item)")
    private Integer quantity;

    public UpdateCartItemRequest() {}

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
