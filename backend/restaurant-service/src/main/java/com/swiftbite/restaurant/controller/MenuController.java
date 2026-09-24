package com.swiftbite.restaurant.controller;

import com.swiftbite.restaurant.dto.ApiResponse;
import com.swiftbite.restaurant.dto.MenuItemResponse;
import com.swiftbite.restaurant.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@Tag(name = "Menu", description = "Public menu item endpoints")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "Get a single menu item by ID")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getMenuItemById(@PathVariable Long id) {
        MenuItemResponse item = menuService.getMenuItemById(id);
        return ResponseEntity.ok(ApiResponse.success("Menu item retrieved successfully", item));
    }

    @GetMapping("/items/batch")
    @Operation(summary = "Get multiple menu items by IDs (for order-service validation)")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenuItemsByIds(
            @RequestParam List<Long> ids) {
        List<MenuItemResponse> items = menuService.getMenuItemsByIds(ids);
        return ResponseEntity.ok(ApiResponse.success("Menu items retrieved successfully", items));
    }
}
