package com.swiftbite.user.controller;

import com.swiftbite.user.dto.request.AddressRequest;
import com.swiftbite.user.dto.request.UpdateProfileRequest;
import com.swiftbite.user.dto.response.AddressResponse;
import com.swiftbite.user.dto.response.ApiResponse;
import com.swiftbite.user.dto.response.UserResponse;
import com.swiftbite.user.entity.User;
import com.swiftbite.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile and address management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get own profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        UserResponse response = userService.getUserById(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update own profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = (User) userDetails;
        UserResponse response = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", response));
    }

    @GetMapping("/{id}/addresses")
    @Operation(summary = "Get addresses for a user")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        checkOwnership(id, userDetails);
        List<AddressResponse> addresses = userService.getUserAddresses(id);
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved", addresses));
    }

    @PostMapping("/{id}/addresses")
    @Operation(summary = "Add an address for a user")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        checkOwnership(id, userDetails);
        AddressResponse address = userService.addAddress(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added", address));
    }

    @PutMapping("/{id}/addresses/{addressId}")
    @Operation(summary = "Update an address for a user")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @PathVariable Long addressId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        checkOwnership(id, userDetails);
        AddressResponse address = userService.updateAddress(id, addressId, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated", address));
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    @Operation(summary = "Delete an address for a user")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long id,
            @PathVariable Long addressId,
            @AuthenticationPrincipal UserDetails userDetails) {
        checkOwnership(id, userDetails);
        userService.deleteAddress(id, addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }

    private void checkOwnership(Long resourceUserId, UserDetails userDetails) {
        User currentUser = (User) userDetails;
        if (!currentUser.getId().equals(resourceUserId)) {
            throw new AccessDeniedException("You can only manage your own resources");
        }
    }
}
