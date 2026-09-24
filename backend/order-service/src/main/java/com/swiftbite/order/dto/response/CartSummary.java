package com.swiftbite.order.dto.response;

import java.math.BigDecimal;

public class CartSummary {

    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal total;

    public CartSummary() {}

    public CartSummary(BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal total) {
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.total = total;
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
