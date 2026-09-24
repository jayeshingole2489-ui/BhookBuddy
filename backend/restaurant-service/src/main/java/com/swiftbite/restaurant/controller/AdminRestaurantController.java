package com.swiftbite.restaurant.controller;

import com.swiftbite.restaurant.dto.ApiResponse;
import com.swiftbite.restaurant.dto.RestaurantRequest;
import com.swiftbite.restaurant.dto.RestaurantResponse;
import com.swiftbite.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/restaurants")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Restaurants", description = "Admin restaurant management endpoints")
public class AdminRestaurantController {

    private final RestaurantService restaurantService;

    public AdminRestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping
    @Operation(summary = "Create a new restaurant")
    public ResponseEntity<ApiResponse<RestaurantResponse>> createRestaurant(
            @Valid @RequestBody RestaurantRequest request) {
        RestaurantResponse created = restaurantService.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Restaurant created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing restaurant")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {
        RestaurantResponse updated = restaurantService.updateRestaurant(id, request);
        return ResponseEntity.ok(ApiResponse.success("Restaurant updated successfully", updated));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle restaurant active/inactive status")
    public ResponseEntity<ApiResponse<RestaurantResponse>> toggleRestaurantStatus(@PathVariable Long id) {
        RestaurantResponse updated = restaurantService.toggleRestaurantStatus(id);
        String statusMsg = updated.isActive() ? "activated" : "deactivated";
        return ResponseEntity.ok(ApiResponse.success("Restaurant " + statusMsg + " successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a restaurant")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant deleted successfully", null));
    }
}
