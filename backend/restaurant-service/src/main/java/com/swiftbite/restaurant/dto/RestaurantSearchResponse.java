package com.swiftbite.restaurant.dto;

import java.util.List;

public class RestaurantSearchResponse {

    private List<RestaurantResponse> restaurants;
    private long total;

    public RestaurantSearchResponse() {
    }

    public RestaurantSearchResponse(List<RestaurantResponse> restaurants, long total) {
        this.restaurants = restaurants;
        this.total = total;
    }

    public List<RestaurantResponse> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<RestaurantResponse> restaurants) {
        this.restaurants = restaurants;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
