package com.swiftbite.restaurant.service;

import com.swiftbite.restaurant.dto.MenuItemRequest;
import com.swiftbite.restaurant.dto.MenuItemResponse;
import com.swiftbite.restaurant.entity.MenuItem;
import com.swiftbite.restaurant.entity.Restaurant;
import com.swiftbite.restaurant.exception.MenuItemNotFoundException;
import com.swiftbite.restaurant.exception.RestaurantNotFoundException;
import com.swiftbite.restaurant.repository.MenuItemRepository;
import com.swiftbite.restaurant.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuService(MenuItemRepository menuItemRepository,
                       RestaurantRepository restaurantRepository) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuByRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundException(id));
        return toResponse(item);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, String category) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        return menuItemRepository.findByRestaurantIdAndCategoryIgnoreCase(restaurantId, category).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        MenuItem item = new MenuItem();
        mapRequestToEntity(request, item);
        item.setRestaurant(restaurant);
        MenuItem saved = menuItemRepository.save(item);
        return toResponse(saved);
    }

    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundException(id));
        mapRequestToEntity(request, item);
        MenuItem saved = menuItemRepository.save(item);
        return toResponse(saved);
    }

    public MenuItemResponse toggleItemAvailability(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundException(id));
        item.setAvailable(!item.isAvailable());
        MenuItem saved = menuItemRepository.save(item);
        return toResponse(saved);
    }

    public void deleteMenuItem(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundException(id));
        menuItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByIds(List<Long> ids) {
        return menuItemRepository.findByIdIn(ids).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void mapRequestToEntity(MenuItemRequest request, MenuItem item) {
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setVegetarian(request.isVegetarian());
        item.setImageUrl(request.getImageUrl());
    }

    public MenuItemResponse toResponse(MenuItem item) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(item.getId());
        response.setRestaurantId(item.getRestaurant().getId());
        response.setRestaurantName(item.getRestaurant().getName());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setPrice(item.getPrice());
        response.setCategory(item.getCategory());
        response.setAvailable(item.isAvailable());
        response.setVegetarian(item.isVegetarian());
        response.setImageUrl(item.getImageUrl());
        return response;
    }
}
