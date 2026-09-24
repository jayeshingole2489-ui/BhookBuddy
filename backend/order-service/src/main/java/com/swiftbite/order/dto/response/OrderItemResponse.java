package com.swiftbite.order.dto.response;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long menuItemId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;

    public OrderItemResponse() {}

    public OrderItemResponse(Long menuItemId, String name, BigDecimal price, Integer quantity, BigDecimal subtotal) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
