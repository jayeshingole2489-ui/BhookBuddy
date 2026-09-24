package com.swiftbite.order.service;

import com.swiftbite.order.client.MenuItemDto;
import com.swiftbite.order.client.RestaurantClient;
import com.swiftbite.order.dto.request.CreateOrderRequest;
import com.swiftbite.order.dto.response.OrderResponse;
import com.swiftbite.order.entity.Cart;
import com.swiftbite.order.entity.CartItem;
import com.swiftbite.order.entity.Order;
import com.swiftbite.order.entity.OrderStatus;
import com.swiftbite.order.exception.InvalidOrderStateException;
import com.swiftbite.order.exception.OrderNotFoundException;
import com.swiftbite.order.kafka.OrderEventProducer;
import com.swiftbite.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private RestaurantClient restaurantClient;

    @Mock
    private OrderEventProducer eventProducer;

    @InjectMocks
    private OrderService orderService;

    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testCart = new Cart(1L);
        testCart.setRestaurantId(10L);
        testCart.setRestaurantName("Test Restaurant");

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setMenuItemId(100L);
        testCartItem.setMenuItemName("Burger");
        testCartItem.setPrice(new BigDecimal("9.99"));
        testCartItem.setQuantity(2);
        testCartItem.setVegetarian(false);
        testCart.addItem(testCartItem);
    }

    @Test
    void createOrder_success() {
        // Given
        Long userId = 1L;
        String userEmail = "user@example.com";
        CreateOrderRequest request = new CreateOrderRequest();
        request.setDeliveryAddress("123 Main St");
        request.setDeliveryCity("Testville");

        MenuItemDto menuItemDto = new MenuItemDto();
        menuItemDto.setId(100L);
        menuItemDto.setName("Burger");
        menuItemDto.setPrice(new BigDecimal("10.99"));
        menuItemDto.setAvailable(true);

        when(cartService.getCartEntity(userId)).thenReturn(testCart);
        when(restaurantClient.getMenuItems(anyList())).thenReturn(List.of(menuItemDto));
        when(restaurantClient.getRestaurant(10L)).thenReturn(null);

        Order savedOrder = buildSavedOrder(userId, userEmail, menuItemDto);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        OrderResponse response = orderService.createOrder(userId, userEmail, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(response.getTotal()).isGreaterThan(BigDecimal.ZERO);

        verify(cartService).clearCart(userId);
        verify(eventProducer).publishOrderEvent(argThat(event ->
                "ORDER_CREATED".equals(event.getEventType())));
    }

    @Test
    void createOrder_emptyCart_throwsIllegalArgumentException() {
        // Given
        Long userId = 2L;
        when(cartService.getCartEntity(userId)).thenReturn(null);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setDeliveryAddress("123 Main St");

        // When / Then
        assertThatThrownBy(() -> orderService.createOrder(userId, "user@test.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cart is empty");

        verify(orderRepository, never()).save(any());
        verify(eventProducer, never()).publishOrderEvent(any());
    }

    @Test
    void cancelOrder_validState_succeeds() {
        // Given
        String orderId = "test-order-uuid";
        Long userId = 1L;

        Order order = new Order();
        order.setId(1L);
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setSubtotal(new BigDecimal("19.98"));
        order.setDeliveryFee(new BigDecimal("2.99"));
        order.setTotal(new BigDecimal("22.97"));
        order.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findByOrderIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        OrderResponse response = orderService.cancelOrder(orderId, userId);

        // Then
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(eventProducer).publishOrderEvent(argThat(event ->
                "ORDER_CANCELLED".equals(event.getEventType())));
    }

    @Test
    void cancelOrder_deliveredState_throwsInvalidOrderStateException() {
        // Given
        String orderId = "test-order-uuid";
        Long userId = 1L;

        Order order = new Order();
        order.setId(1L);
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findByOrderIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        // When / Then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("DELIVERED");

        verify(orderRepository, never()).save(any());
        verify(eventProducer, never()).publishOrderEvent(any());
    }

    @Test
    void cancelOrder_orderNotOwned_throwsOrderNotFoundException() {
        // Given
        String orderId = "test-order-uuid";
        Long userId = 99L;

        when(orderRepository.findByOrderIdAndUserId(orderId, userId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // Helper to build a saved order with a DB id (simulates JPA-assigned id)
    private Order buildSavedOrder(Long userId, String userEmail, MenuItemDto menuItemDto) {
        Order order = new Order();
        order.setId(1L);
        order.setOrderId("generated-uuid");
        order.setUserId(userId);
        order.setUserEmail(userEmail);
        order.setRestaurantId(10L);
        order.setRestaurantName("Test Restaurant");
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setDeliveryAddress("123 Main St");
        order.setDeliveryFee(new BigDecimal("2.99"));

        com.swiftbite.order.entity.OrderItem item = new com.swiftbite.order.entity.OrderItem(
                order, menuItemDto.getId(), menuItemDto.getName(), menuItemDto.getPrice(), 2);
        order.addItem(item);

        BigDecimal subtotal = menuItemDto.getPrice().multiply(BigDecimal.valueOf(2));
        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(new BigDecimal("2.99")));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }
}
