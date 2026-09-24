package com.swiftbite.restaurant.controller;

import com.swiftbite.restaurant.dto.ApiResponse;
import com.swiftbite.restaurant.dto.MenuItemRequest;
import com.swiftbite.restaurant.dto.MenuItemResponse;
import com.swiftbite.restaurant.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/menu")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Menu", description = "Admin menu item management endpoints")
public class AdminMenuController {

    private final MenuService menuService;

    public AdminMenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping("/{restaurantId}/items")
    @Operation(summary = "Add a menu item to a restaurant")
    public ResponseEntity<ApiResponse<MenuItemResponse>> addMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        MenuItemResponse created = menuService.addMenuItem(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu item added successfully", created));
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Update a menu item")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        MenuItemResponse updated = menuService.updateMenuItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Menu item updated successfully", updated));
    }

    @PatchMapping("/items/{id}/toggle")
    @Operation(summary = "Toggle menu item availability")
    public ResponseEntity<ApiResponse<MenuItemResponse>> toggleItemAvailability(@PathVariable Long id) {
        MenuItemResponse updated = menuService.toggleItemAvailability(id);
        String statusMsg = updated.isAvailable() ? "available" : "unavailable";
        return ResponseEntity.ok(ApiResponse.success("Menu item marked as " + statusMsg, updated));
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Delete a menu item")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.success("Menu item deleted successfully", null));
    }
}
