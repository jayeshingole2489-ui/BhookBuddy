package com.swiftbite.order.client;

import java.math.BigDecimal;

public class RestaurantDto {

    private Long id;
    private String name;
    private String address;
    private String city;
    private boolean open;
    private BigDecimal deliveryFee;

    public RestaurantDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
}
