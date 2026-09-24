package com.swiftbite.order.client;

import com.swiftbite.order.exception.RestaurantServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Component
public class RestaurantClient {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantClient.class);

    private final RestTemplate restTemplate;
    private final String restaurantServiceUrl;

    public RestaurantClient(RestTemplate restTemplate,
                            @Value("${services.restaurant-service}") String restaurantServiceUrl) {
        this.restTemplate = restTemplate;
        this.restaurantServiceUrl = restaurantServiceUrl;
    }

    public List<MenuItemDto> getMenuItems(List<Long> ids) {
        try {
            String idsParam = ids.stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            String url = UriComponentsBuilder
                    .fromHttpUrl(restaurantServiceUrl + "/api/menu/items/batch")
                    .queryParam("ids", idsParam)
                    .toUriString();

            ResponseEntity<ApiResponse<List<MenuItemDto>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<List<MenuItemDto>>>() {}
            );
            ApiResponse<List<MenuItemDto>> body = response.getBody();
            if (body != null && body.isSuccess() && body.getData() != null) {
                return body.getData();
            }
            return Collections.emptyList();
        } catch (ResourceAccessException ex) {
            logger.warn("Restaurant service is unavailable when fetching menu items: {}", ex.getMessage());
            throw new RestaurantServiceException("Restaurant service is currently unavailable", ex);
        } catch (Exception ex) {
            logger.error("Error calling restaurant service for menu items: {}", ex.getMessage(), ex);
            throw new RestaurantServiceException("Failed to retrieve menu item details", ex);
        }
    }

    public RestaurantDto getRestaurant(Long id) {
        try {
            String url = restaurantServiceUrl + "/api/restaurants/" + id;
            ResponseEntity<ApiResponse<RestaurantDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<RestaurantDto>>() {}
            );
            ApiResponse<RestaurantDto> body = response.getBody();
            return (body != null && body.isSuccess()) ? body.getData() : null;
        } catch (ResourceAccessException ex) {
            logger.warn("Restaurant service is unavailable when fetching restaurant {}: {}", id, ex.getMessage());
            throw new RestaurantServiceException("Restaurant service is currently unavailable", ex);
        } catch (Exception ex) {
            logger.error("Error calling restaurant service for restaurant {}: {}", id, ex.getMessage(), ex);
            throw new RestaurantServiceException("Failed to retrieve restaurant details", ex);
        }
    }
}
