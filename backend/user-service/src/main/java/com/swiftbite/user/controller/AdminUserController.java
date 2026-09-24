package com.swiftbite.user.controller;

import com.swiftbite.user.dto.response.ApiResponse;
import com.swiftbite.user.dto.response.UserResponse;
import com.swiftbite.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Users", description = "Admin endpoints for user management")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all users (admin)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (admin)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved", user));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Enable or disable a user (admin)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        UserResponse user = userService.updateUserStatus(id, active);
        String message = active ? "User enabled successfully" : "User disabled successfully";
        return ResponseEntity.ok(ApiResponse.success(message, user));
    }
}
