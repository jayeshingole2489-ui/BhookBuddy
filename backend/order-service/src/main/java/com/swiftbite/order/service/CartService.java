package com.swiftbite.order.service;

import com.swiftbite.order.client.MenuItemDto;
import com.swiftbite.order.client.RestaurantClient;
import com.swiftbite.order.client.RestaurantDto;
import com.swiftbite.order.dto.request.AddToCartRequest;
import com.swiftbite.order.dto.response.CartItemResponse;
import com.swiftbite.order.dto.response.CartResponse;
import com.swiftbite.order.dto.response.CartSummary;
import com.swiftbite.order.entity.Cart;
import com.swiftbite.order.entity.CartItem;
import com.swiftbite.order.exception.CartRestaurantConflictException;
import com.swiftbite.order.exception.RestaurantServiceException;
import com.swiftbite.order.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private static final BigDecimal DEFAULT_DELIVERY_FEE = new BigDecimal("2.99");

    private final CartRepository cartRepository;
    private final RestaurantClient restaurantClient;

    public CartService(CartRepository cartRepository, RestaurantClient restaurantClient) {
        this.cartRepository = cartRepository;
        this.restaurantClient = restaurantClient;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(this::toCartResponse)
                .orElseGet(() -> emptyCartResponse());
    }

    public CartResponse addItem(Long userId, AddToCartRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> new Cart(userId));

        // Validate restaurant consistency
        if (!cart.isEmpty() && cart.getRestaurantId() != null
                && !cart.getRestaurantId().equals(request.getRestaurantId())) {
            throw new CartRestaurantConflictException(
                    "Cart has items from another restaurant. Clear cart first.");
        }

        // Fetch item details from restaurant-service
        MenuItemDto menuItem = fetchMenuItemWithFallback(request.getMenuItemId());

        if (menuItem != null && !menuItem.isAvailable()) {
            throw new IllegalArgumentException("Menu item '" + menuItem.getName() + "' is currently unavailable");
        }

        // If restaurant service was unavailable, get restaurant info from cart or skip
        if (cart.getRestaurantId() == null) {
            cart.setRestaurantId(request.getRestaurantId());
            try {
                RestaurantDto restaurant = restaurantClient.getRestaurant(request.getRestaurantId());
                if (restaurant != null) {
                    cart.setRestaurantName(restaurant.getName());
                    if (restaurant.getDeliveryFee() != null) {
                        cart.setDeliveryFee(restaurant.getDeliveryFee());
                    }
                }
            } catch (RestaurantServiceException ex) {
                logger.warn("Could not fetch restaurant details for id {}: {}", request.getRestaurantId(), ex.getMessage());
                cart.setRestaurantName("Restaurant #" + request.getRestaurantId());
            }
        }

        // Check if item already in cart — update quantity instead
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getMenuItemId().equals(request.getMenuItemId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            if (menuItem != null) {
                item.setPrice(menuItem.getPrice());
                item.setMenuItemName(menuItem.getName());
            }
        } else {
            CartItem newItem = new CartItem();
            newItem.setMenuItemId(request.getMenuItemId());
            newItem.setQuantity(request.getQuantity());

            if (menuItem != null) {
                newItem.setMenuItemName(menuItem.getName());
                newItem.setPrice(menuItem.getPrice());
                newItem.setVegetarian(menuItem.isVegetarian());
                newItem.setImageUrl(menuItem.getImageUrl());
            } else {
                // Fallback — price unknown, set to zero; will be recalculated at order time
                newItem.setMenuItemName("Item #" + request.getMenuItemId());
                newItem.setPrice(BigDecimal.ZERO);
            }
            cart.addItem(newItem);
        }

        Cart saved = cartRepository.save(cart);
        return toCartResponse(saved);
    }

    public CartResponse updateItemQuantity(Long userId, Long cartItemId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        if (quantity <= 0) {
            cart.removeItem(item);
        } else {
            item.setQuantity(quantity);
        }

        if (cart.isEmpty()) {
            cart.setRestaurantId(null);
            cart.setRestaurantName(null);
        }

        Cart saved = cartRepository.save(cart);
        return toCartResponse(saved);
    }

    public CartResponse removeItem(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        cart.removeItem(item);

        if (cart.isEmpty()) {
            cart.setRestaurantId(null);
            cart.setRestaurantName(null);
        }

        Cart saved = cartRepository.save(cart);
        return toCartResponse(saved);
    }

    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setRestaurantId(null);
            cart.setRestaurantName(null);
            cartRepository.save(cart);
        });
    }

    @Transactional(readOnly = true)
    public CartSummary getCartTotal(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null || cart.isEmpty()) {
            return new CartSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal subtotal = calculateSubtotal(cart);
        BigDecimal deliveryFee = cart.getDeliveryFee() != null ? cart.getDeliveryFee() : DEFAULT_DELIVERY_FEE;
        BigDecimal total = subtotal.add(deliveryFee);
        return new CartSummary(subtotal, deliveryFee, total);
    }

    // ---- Package-visible for OrderService ----

    Cart getCartEntity(Long userId) {
        return cartRepository.findByUserId(userId).orElse(null);
    }

    // ---- Private helpers ----

    private MenuItemDto fetchMenuItemWithFallback(Long menuItemId) {
        try {
            List<MenuItemDto> items = restaurantClient.getMenuItems(Collections.singletonList(menuItemId));
            if (items != null && !items.isEmpty()) {
                return items.get(0);
            }
        } catch (RestaurantServiceException ex) {
            logger.warn("Restaurant service unavailable when fetching menu item {}: {}", menuItemId, ex.getMessage());
        }
        return null;
    }

    private BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartResponse toCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setRestaurantId(cart.getRestaurantId());
        response.setRestaurantName(cart.getRestaurantName());

        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getMenuItemId(),
                        item.getMenuItemName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getSubtotal(),
                        item.isVegetarian(),
                        item.getImageUrl()
                ))
                .collect(Collectors.toList());

        response.setItems(itemResponses);

        BigDecimal subtotal = calculateSubtotal(cart);
        BigDecimal deliveryFee = cart.isEmpty() ? BigDecimal.ZERO
                : (cart.getDeliveryFee() != null ? cart.getDeliveryFee() : DEFAULT_DELIVERY_FEE);
        response.setSubtotal(subtotal);
        response.setDeliveryFee(deliveryFee);
        response.setTotal(subtotal.add(deliveryFee));
        response.setItemCount(cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum());

        return response;
    }

    private CartResponse emptyCartResponse() {
        CartResponse response = new CartResponse();
        response.setItems(Collections.emptyList());
        response.setSubtotal(BigDecimal.ZERO);
        response.setDeliveryFee(BigDecimal.ZERO);
        response.setTotal(BigDecimal.ZERO);
        response.setItemCount(0);
        return response;
    }
}
