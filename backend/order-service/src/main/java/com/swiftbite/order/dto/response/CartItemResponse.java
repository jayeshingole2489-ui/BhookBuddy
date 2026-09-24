package com.swiftbite.order.dto.response;

import java.math.BigDecimal;

public class CartItemResponse {

    private Long id;
    private Long menuItemId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private boolean vegetarian;
    private String imageUrl;

    public CartItemResponse() {}

    public CartItemResponse(Long id, Long menuItemId, String name, BigDecimal price,
                            Integer quantity, BigDecimal subtotal, boolean vegetarian, String imageUrl) {
        this.id = id;
        this.menuItemId = menuItemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.vegetarian = vegetarian;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public boolean isVegetarian() { return vegetarian; }
    public void setVegetarian(boolean vegetarian) { this.vegetarian = vegetarian; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
