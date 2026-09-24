package com.swiftbite.restaurant.controller;

import com.swiftbite.restaurant.dto.ApiResponse;
import com.swiftbite.restaurant.dto.MenuItemResponse;
import com.swiftbite.restaurant.dto.RestaurantResponse;
import com.swiftbite.restaurant.dto.RestaurantSearchResponse;
import com.swiftbite.restaurant.service.MenuService;
import com.swiftbite.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurants", description = "Public restaurant endpoints")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;

    public RestaurantController(RestaurantService restaurantService, MenuService menuService) {
        this.restaurantService = restaurantService;
        this.menuService = menuService;
    }

    @GetMapping
    @Operation(summary = "Get all restaurants with optional filters")
    public ResponseEntity<ApiResponse<RestaurantSearchResponse>> getAllRestaurants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String city) {
        RestaurantSearchResponse result = restaurantService.getAllRestaurants(search, cuisine, city);
        return ResponseEntity.ok(ApiResponse.success("Restaurants retrieved successfully", result));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active restaurants")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getActiveRestaurants() {
        List<RestaurantResponse> restaurants = restaurantService.getActiveRestaurants();
        return ResponseEntity.ok(ApiResponse.success("Active restaurants retrieved successfully", restaurants));
    }

    @GetMapping("/search")
    @Operation(summary = "Search restaurants by name, cuisine, or city")
    public ResponseEntity<ApiResponse<RestaurantSearchResponse>> searchRestaurants(
            @RequestParam(name = "q", required = false) String query) {
        RestaurantSearchResponse result = restaurantService.searchRestaurants(query);
        return ResponseEntity.ok(ApiResponse.success("Search completed", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant by ID")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurantById(@PathVariable Long id) {
        RestaurantResponse restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant retrieved successfully", restaurant));
    }

    @GetMapping("/{id}/menu")
    @Operation(summary = "Get menu items for a restaurant")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getRestaurantMenu(@PathVariable Long id) {
        List<MenuItemResponse> menu = menuService.getMenuByRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success("Menu retrieved successfully", menu));
    }
}
