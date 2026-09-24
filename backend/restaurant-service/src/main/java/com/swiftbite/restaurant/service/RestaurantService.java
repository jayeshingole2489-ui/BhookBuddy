package com.swiftbite.restaurant.service;

import com.swiftbite.restaurant.dto.RestaurantRequest;
import com.swiftbite.restaurant.dto.RestaurantResponse;
import com.swiftbite.restaurant.dto.RestaurantSearchResponse;
import com.swiftbite.restaurant.entity.Restaurant;
import com.swiftbite.restaurant.exception.RestaurantNotFoundException;
import com.swiftbite.restaurant.repository.MenuItemRepository;
import com.swiftbite.restaurant.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional(readOnly = true)
    public RestaurantSearchResponse getAllRestaurants(String search, String cuisineType, String city) {
        String searchParam = StringUtils.hasText(search) ? search : null;
        String cuisineParam = StringUtils.hasText(cuisineType) ? cuisineType : null;
        String cityParam = StringUtils.hasText(city) ? city : null;

        List<Restaurant> restaurants = restaurantRepository.findWithFilters(searchParam, cuisineParam, cityParam);
        List<RestaurantResponse> responses = restaurants.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new RestaurantSearchResponse(responses, responses.size());
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
        return toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getActiveRestaurants() {
        return restaurantRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        mapRequestToEntity(request, restaurant);
        Restaurant saved = restaurantRepository.save(restaurant);
        return toResponse(saved);
    }

    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
        mapRequestToEntity(request, restaurant);
        Restaurant saved = restaurantRepository.save(restaurant);
        return toResponse(saved);
    }

    public RestaurantResponse toggleRestaurantStatus(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
        restaurant.setActive(!restaurant.isActive());
        Restaurant saved = restaurantRepository.save(restaurant);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RestaurantSearchResponse searchRestaurants(String query) {
        if (!StringUtils.hasText(query)) {
            List<RestaurantResponse> all = getActiveRestaurants();
            return new RestaurantSearchResponse(all, all.size());
        }
        List<Restaurant> results = restaurantRepository.searchActiveRestaurants(query);
        List<RestaurantResponse> responses = results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new RestaurantSearchResponse(responses, responses.size());
    }

    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
        restaurantRepository.delete(restaurant);
    }

    private void mapRequestToEntity(RestaurantRequest request, Restaurant restaurant) {
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setPhone(request.getPhone());
        restaurant.setEmail(request.getEmail());
        if (request.getDeliveryFee() != null) {
            restaurant.setDeliveryFee(request.getDeliveryFee());
        }
        if (request.getMinOrderAmount() != null) {
            restaurant.setMinOrderAmount(request.getMinOrderAmount());
        }
        if (request.getEstimatedDeliveryMinutes() != null) {
            restaurant.setEstimatedDeliveryMinutes(request.getEstimatedDeliveryMinutes());
        }
        restaurant.setImageUrl(request.getImageUrl());
    }

    public RestaurantResponse toResponse(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setDescription(restaurant.getDescription());
        response.setCuisineType(restaurant.getCuisineType());
        response.setAddress(restaurant.getAddress());
        response.setCity(restaurant.getCity());
        response.setPhone(restaurant.getPhone());
        response.setEmail(restaurant.getEmail());
        response.setRating(restaurant.getRating());
        response.setActive(restaurant.isActive());
        response.setDeliveryFee(restaurant.getDeliveryFee());
        response.setMinOrderAmount(restaurant.getMinOrderAmount());
        response.setEstimatedDeliveryMinutes(restaurant.getEstimatedDeliveryMinutes());
        response.setImageUrl(restaurant.getImageUrl());
        response.setCreatedAt(restaurant.getCreatedAt());

        long count = menuItemRepository.countByRestaurantId(restaurant.getId());
        response.setMenuItemCount((int) count);

        return response;
    }
}
