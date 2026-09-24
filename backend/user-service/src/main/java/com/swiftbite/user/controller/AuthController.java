package com.swiftbite.user.controller;

import com.swiftbite.user.dto.request.LoginRequest;
import com.swiftbite.user.dto.request.RegisterRequest;
import com.swiftbite.user.dto.response.ApiResponse;
import com.swiftbite.user.dto.response.AuthResponse;
import com.swiftbite.user.dto.response.UserResponse;
import com.swiftbite.user.entity.User;
import com.swiftbite.user.service.AuthService;
import com.swiftbite.user.service.JwtService;
import com.swiftbite.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register, login and token validation endpoints")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthService authService, JwtService jwtService, UserService userService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token and return current user info")
    public ResponseEntity<ApiResponse<UserResponse>> validate(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails instanceof User user) {
            UserResponse userResponse = userService.getUserById(user.getId());
            return ResponseEntity.ok(ApiResponse.success("Token is valid", userResponse));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid or missing token"));
    }
}
