package com.swiftbite.order.controller;

import com.swiftbite.order.dto.request.AddToCartRequest;
import com.swiftbite.order.dto.request.UpdateCartItemRequest;
import com.swiftbite.order.dto.response.ApiResponse;
import com.swiftbite.order.dto.response.CartResponse;
import com.swiftbite.order.security.AuthenticatedUser;
import com.swiftbite.order.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart management")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal AuthenticatedUser user) {
        CartResponse cart = cartService.getCart(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse cart = cartService.addItem(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update item quantity in cart")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse cart = cartService.updateItemQuantity(user.getUserId(), cartItemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cart));
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long cartItemId) {
        CartResponse cart = cartService.removeItem(user.getUserId(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    @Operation(summary = "Clear the entire cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal AuthenticatedUser user) {
        cartService.clearCart(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
