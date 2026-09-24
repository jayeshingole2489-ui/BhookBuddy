package com.swiftbite.order.service;

import com.swiftbite.order.client.MenuItemDto;
import com.swiftbite.order.client.RestaurantClient;
import com.swiftbite.order.client.RestaurantDto;
import com.swiftbite.order.dto.request.CreateOrderRequest;
import com.swiftbite.order.dto.response.OrderItemResponse;
import com.swiftbite.order.dto.response.OrderResponse;
import com.swiftbite.order.entity.Cart;
import com.swiftbite.order.entity.CartItem;
import com.swiftbite.order.entity.Order;
import com.swiftbite.order.entity.OrderItem;
import com.swiftbite.order.entity.OrderStatus;
import com.swiftbite.order.event.DeliveryEvent;
import com.swiftbite.order.event.OrderEvent;
import com.swiftbite.order.event.PaymentEvent;
import com.swiftbite.order.exception.InvalidOrderStateException;
import com.swiftbite.order.exception.OrderNotFoundException;
import com.swiftbite.order.exception.RestaurantServiceException;
import com.swiftbite.order.kafka.OrderEventProducer;
import com.swiftbite.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("2.99");

    private static final Set<OrderStatus> CANCELLABLE_STATUSES = Set.of(
            OrderStatus.CREATED,
            OrderStatus.PAYMENT_PENDING,
            OrderStatus.CONFIRMED
    );

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final RestaurantClient restaurantClient;
    private final OrderEventProducer eventProducer;

    public OrderService(OrderRepository orderRepository,
                        CartService cartService,
                        RestaurantClient restaurantClient,
                        OrderEventProducer eventProducer) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.restaurantClient = restaurantClient;
        this.eventProducer = eventProducer;
    }

    public OrderResponse createOrder(Long userId, String userEmail, CreateOrderRequest request) {
        // 1. Load the cart
        Cart cart = cartService.getCartEntity(userId);
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Cannot create order: cart is empty");
        }

        // 2. Fetch authoritative prices from restaurant service
        List<Long> menuItemIds = cart.getItems().stream()
                .map(CartItem::getMenuItemId)
                .collect(Collectors.toList());

        Map<Long, MenuItemDto> menuItemMap = fetchMenuItems(menuItemIds);

        // 3. Get restaurant info (name + authoritative delivery fee)
        String restaurantName = cart.getRestaurantName();
        BigDecimal deliveryFee = cart.getDeliveryFee() != null ? cart.getDeliveryFee() : DELIVERY_FEE;
        try {
            RestaurantDto restaurant = restaurantClient.getRestaurant(cart.getRestaurantId());
            if (restaurant != null) {
                restaurantName = restaurant.getName();
                if (restaurant.getDeliveryFee() != null) {
                    deliveryFee = restaurant.getDeliveryFee();
                }
            }
        } catch (RestaurantServiceException ex) {
            logger.warn("Could not refresh restaurant info for order creation, using cached data: {}", ex.getMessage());
        }

        // 4. Build order with server-side price calculation
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setUserEmail(userEmail);
        order.setRestaurantId(cart.getRestaurantId());
        order.setRestaurantName(restaurantName);
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setDeliveryCity(request.getDeliveryCity());
        order.setNotes(request.getNotes());
        order.setDeliveryFee(deliveryFee);

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            MenuItemDto menuItemDto = menuItemMap.get(cartItem.getMenuItemId());

            // Use authoritative price from restaurant service; fall back to cart price if unavailable
            BigDecimal unitPrice;
            String itemName;
            if (menuItemDto != null) {
                if (!menuItemDto.isAvailable()) {
                    throw new IllegalArgumentException(
                            "Menu item '" + menuItemDto.getName() + "' is no longer available");
                }
                unitPrice = menuItemDto.getPrice();
                itemName = menuItemDto.getName();
            } else {
                logger.warn("Could not verify price for menu item {}; using cached cart price", cartItem.getMenuItemId());
                unitPrice = cartItem.getPrice();
                itemName = cartItem.getMenuItemName();
            }

            OrderItem orderItem = new OrderItem(order, cartItem.getMenuItemId(), itemName, unitPrice, cartItem.getQuantity());
            orderItems.add(orderItem);
            subtotal = subtotal.add(orderItem.getSubtotal());
        }

        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(deliveryFee));

        for (OrderItem item : orderItems) {
            order.addItem(item);
        }

        Order savedOrder = orderRepository.save(order);

        // 5. Clear cart
        cartService.clearCart(userId);

        // 6. Publish event
        publishOrderEvent(savedOrder, "ORDER_CREATED");

        logger.info("Order created: orderId={}, userId={}, total={}", savedOrder.getOrderId(), userId, savedOrder.getTotal());
        return toOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId, Long userId) {
        Order order = orderRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId, userId));
        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse cancelOrder(String orderId, Long userId) {
        Order order = orderRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId, userId));

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new InvalidOrderStateException(
                    "Order cannot be cancelled in " + order.getStatus() + " state");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        publishOrderEvent(saved, "ORDER_CANCELLED");
        logger.info("Order cancelled: orderId={}, userId={}", orderId, userId);

        return toOrderResponse(saved);
    }

    public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        logger.info("Order status updated by admin: orderId={}, newStatus={}", orderId, newStatus);
        return toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    public void processPaymentEvent(PaymentEvent event) {
        Order order = orderRepository.findByOrderId(event.getOrderId()).orElse(null);
        if (order == null) {
            logger.warn("Received payment event for unknown orderId: {}", event.getOrderId());
            return;
        }

        String eventType = event.getEventType();
        if ("PAYMENT_COMPLETED".equals(eventType)) {
            order.setPaymentId(event.getPaymentId());
            order.setStatus(OrderStatus.CONFIRMED);
            Order saved = orderRepository.save(order);
            publishOrderEvent(saved, "ORDER_CONFIRMED");
            logger.info("Order confirmed after payment: orderId={}", event.getOrderId());

        } else if ("PAYMENT_FAILED".equals(eventType)) {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            Order saved = orderRepository.save(order);
            publishOrderEvent(saved, "ORDER_PAYMENT_FAILED");
            logger.warn("Order payment failed: orderId={}, reason={}", event.getOrderId(), event.getFailureReason());

        } else {
            logger.warn("Unknown payment event type: {}", eventType);
        }
    }

    public void processDeliveryEvent(DeliveryEvent event) {
        Order order = orderRepository.findByOrderId(event.getOrderId()).orElse(null);
        if (order == null) {
            logger.warn("Received delivery event for unknown orderId: {}", event.getOrderId());
            return;
        }

        String eventType = event.getEventType();
        switch (eventType) {
            case "DELIVERY_CREATED":
                order.setDeliveryId(event.getDeliveryId());
                orderRepository.save(order);
                logger.info("Delivery created for order: orderId={}, deliveryId={}",
                        event.getOrderId(), event.getDeliveryId());
                break;

            case "DELIVERY_ASSIGNED":
                order.setDeliveryId(event.getDeliveryId());
                order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
                orderRepository.save(order);
                logger.info("Order out for delivery: orderId={}", event.getOrderId());
                break;

            case "DELIVERY_PICKED_UP":
                order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
                orderRepository.save(order);
                logger.info("Order picked up: orderId={}", event.getOrderId());
                break;

            case "DELIVERY_DELIVERED":
                order.setStatus(OrderStatus.DELIVERED);
                Order saved = orderRepository.save(order);
                publishOrderEvent(saved, "ORDER_DELIVERED");
                logger.info("Order delivered: orderId={}", event.getOrderId());
                break;

            case "DELIVERY_CANCELLED":
                order.setStatus(OrderStatus.CANCELLED);
                Order cancelledOrder = orderRepository.save(order);
                publishOrderEvent(cancelledOrder, "ORDER_CANCELLED");
                logger.info("Order cancelled due to delivery cancellation: orderId={}", event.getOrderId());
                break;

            default:
                logger.warn("Unknown delivery event type: {}", eventType);
        }
    }

    // ---- Private helpers ----

    private Map<Long, MenuItemDto> fetchMenuItems(List<Long> ids) {
        try {
            List<MenuItemDto> items = restaurantClient.getMenuItems(ids);
            if (items != null) {
                return items.stream()
                        .collect(Collectors.toMap(MenuItemDto::getId, Function.identity()));
            }
        } catch (RestaurantServiceException ex) {
            logger.warn("Could not fetch menu items from restaurant service: {}", ex.getMessage());
        }
        return Map.of();
    }

    private void publishOrderEvent(Order order, String eventType) {
        try {
            OrderEvent event = new OrderEvent();
            event.setEventId(UUID.randomUUID().toString());
            event.setEventType(eventType);
            event.setOrderId(order.getOrderId());
            event.setOrderDbId(order.getId());
            event.setUserId(order.getUserId());
            event.setUserEmail(order.getUserEmail());
            event.setRestaurantId(order.getRestaurantId());
            event.setRestaurantName(order.getRestaurantName());
            event.setTotal(order.getTotal());
            event.setDeliveryAddress(order.getDeliveryAddress());
            event.setStatus(order.getStatus());
            event.setTimestamp(LocalDateTime.now());
            event.setCorrelationId(order.getOrderId());
            eventProducer.publishOrderEvent(event);
        } catch (Exception ex) {
            logger.error("Failed to publish {} event for order {}: {}", eventType, order.getOrderId(), ex.getMessage(), ex);
        }
    }

    private OrderResponse toOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderId(order.getOrderId());
        response.setRestaurantId(order.getRestaurantId());
        response.setRestaurantName(order.getRestaurantName());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setSubtotal(order.getSubtotal());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setTotal(order.getTotal());
        response.setStatus(order.getStatus());
        response.setPaymentId(order.getPaymentId());
        response.setDeliveryId(order.getDeliveryId());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getMenuItemId(),
                        item.getName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());
        response.setItems(items);

        return response;
    }
}
