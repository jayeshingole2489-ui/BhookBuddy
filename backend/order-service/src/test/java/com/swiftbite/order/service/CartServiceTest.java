package com.swiftbite.order.service;

import com.swiftbite.order.client.MenuItemDto;
import com.swiftbite.order.client.RestaurantClient;
import com.swiftbite.order.client.RestaurantDto;
import com.swiftbite.order.dto.request.AddToCartRequest;
import com.swiftbite.order.dto.response.CartResponse;
import com.swiftbite.order.entity.Cart;
import com.swiftbite.order.entity.CartItem;
import com.swiftbite.order.exception.CartRestaurantConflictException;
import com.swiftbite.order.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RestaurantClient restaurantClient;

    @InjectMocks
    private CartService cartService;

    private MenuItemDto mockMenuItemDto;

    @BeforeEach
    void setUp() {
        mockMenuItemDto = new MenuItemDto();
        mockMenuItemDto.setId(100L);
        mockMenuItemDto.setName("Burger");
        mockMenuItemDto.setPrice(new BigDecimal("9.99"));
        mockMenuItemDto.setAvailable(true);
        mockMenuItemDto.setVegetarian(false);
        mockMenuItemDto.setImageUrl("https://example.com/burger.jpg");
        mockMenuItemDto.setRestaurantId(10L);
    }

    @Test
    void addItem_emptyCart_createsCartAndAddsItem() {
        // Given
        Long userId = 1L;
        AddToCartRequest request = new AddToCartRequest();
        request.setRestaurantId(10L);
        request.setMenuItemId(100L);
        request.setQuantity(2);

        RestaurantDto restaurantDto = new RestaurantDto();
        restaurantDto.setId(10L);
        restaurantDto.setName("Test Restaurant");

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(restaurantClient.getMenuItems(anyList())).thenReturn(List.of(mockMenuItemDto));
        when(restaurantClient.getRestaurant(10L)).thenReturn(restaurantDto);

        Cart savedCart = buildSavedCart(userId, 10L, "Test Restaurant", 100L, "Burger",
                new BigDecimal("9.99"), 2);
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        // When
        CartResponse response = cartService.addItem(userId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getName()).isEqualTo("Burger");
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("19.98"));
    }

    @Test
    void addItem_differentRestaurant_throwsCartRestaurantConflictException() {
        // Given
        Long userId = 1L;
        Cart existingCart = new Cart(userId);
        existingCart.setRestaurantId(10L);
        existingCart.setRestaurantName("Restaurant A");

        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setMenuItemId(50L);
        existingItem.setMenuItemName("Pizza");
        existingItem.setPrice(new BigDecimal("12.00"));
        existingItem.setQuantity(1);
        existingCart.addItem(existingItem);

        AddToCartRequest request = new AddToCartRequest();
        request.setRestaurantId(20L); // Different restaurant!
        request.setMenuItemId(100L);
        request.setQuantity(1);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

        // When / Then
        assertThatThrownBy(() -> cartService.addItem(userId, request))
                .isInstanceOf(CartRestaurantConflictException.class)
                .hasMessageContaining("Clear cart first");

        verify(cartRepository, never()).save(any());
    }

    @Test
    void updateItemQuantity_setsNewQuantity() {
        // Given
        Long userId = 1L;
        Long cartItemId = 5L;

        Cart cart = new Cart(userId);
        cart.setRestaurantId(10L);

        CartItem item = new CartItem();
        item.setId(cartItemId);
        item.setMenuItemId(100L);
        item.setMenuItemName("Burger");
        item.setPrice(new BigDecimal("9.99"));
        item.setQuantity(1);
        cart.addItem(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        Cart updatedCart = buildSavedCart(userId, 10L, null, 100L, "Burger", new BigDecimal("9.99"), 3);
        when(cartRepository.save(any(Cart.class))).thenReturn(updatedCart);

        // When
        CartResponse response = cartService.updateItemQuantity(userId, cartItemId, 3);

        // Then
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(3);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void clearCart_removesAllItems() {
        // Given
        Long userId = 1L;
        Cart cart = new Cart(userId);
        cart.setRestaurantId(10L);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setMenuItemId(100L);
        item.setMenuItemName("Burger");
        item.setPrice(new BigDecimal("9.99"));
        item.setQuantity(2);
        cart.addItem(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        cartService.clearCart(userId);

        // Then
        verify(cartRepository).save(argThat(c -> c.isEmpty() && c.getRestaurantId() == null));
    }

    @Test
    void addItem_sameItemTwice_incrementsQuantity() {
        // Given
        Long userId = 1L;
        Cart existingCart = new Cart(userId);
        existingCart.setRestaurantId(10L);
        existingCart.setRestaurantName("Test Restaurant");

        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setMenuItemId(100L);
        existingItem.setMenuItemName("Burger");
        existingItem.setPrice(new BigDecimal("9.99"));
        existingItem.setQuantity(1);
        existingCart.addItem(existingItem);

        AddToCartRequest request = new AddToCartRequest();
        request.setRestaurantId(10L);
        request.setMenuItemId(100L);
        request.setQuantity(2);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(restaurantClient.getMenuItems(anyList())).thenReturn(List.of(mockMenuItemDto));

        Cart savedCart = buildSavedCart(userId, 10L, "Test Restaurant", 100L, "Burger",
                new BigDecimal("9.99"), 3);
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        // When
        CartResponse response = cartService.addItem(userId, request);

        // Then
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(3);
    }

    // Helper to build a cart that simulates what the repository would return after save
    private Cart buildSavedCart(Long userId, Long restaurantId, String restaurantName,
                                 Long menuItemId, String itemName, BigDecimal price, int quantity) {
        Cart cart = new Cart(userId);
        cart.setRestaurantId(restaurantId);
        cart.setRestaurantName(restaurantName);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setMenuItemId(menuItemId);
        item.setMenuItemName(itemName);
        item.setPrice(price);
        item.setQuantity(quantity);
        cart.addItem(item);
        return cart;
    }
}
