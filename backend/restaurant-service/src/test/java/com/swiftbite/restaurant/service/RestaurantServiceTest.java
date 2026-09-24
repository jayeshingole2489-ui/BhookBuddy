package com.swiftbite.restaurant.service;

import com.swiftbite.restaurant.dto.RestaurantRequest;
import com.swiftbite.restaurant.dto.RestaurantResponse;
import com.swiftbite.restaurant.dto.RestaurantSearchResponse;
import com.swiftbite.restaurant.entity.Restaurant;
import com.swiftbite.restaurant.exception.RestaurantNotFoundException;
import com.swiftbite.restaurant.repository.MenuItemRepository;
import com.swiftbite.restaurant.repository.RestaurantRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant sampleRestaurant;
    private RestaurantRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRestaurant = new Restaurant();
        sampleRestaurant.setId(1L);
        sampleRestaurant.setName("Spice Garden");
        sampleRestaurant.setDescription("Authentic Indian cuisine");
        sampleRestaurant.setCuisineType("Indian");
        sampleRestaurant.setCity("Chennai");
        sampleRestaurant.setAddress("45 Anna Salai");
        sampleRestaurant.setPhone("+91-9876543210");
        sampleRestaurant.setEmail("spicegarden@swiftbite.com");
        sampleRestaurant.setRating(new BigDecimal("4.3"));
        sampleRestaurant.setActive(true);
        sampleRestaurant.setDeliveryFee(new BigDecimal("30.00"));
        sampleRestaurant.setMinOrderAmount(new BigDecimal("150.00"));
        sampleRestaurant.setEstimatedDeliveryMinutes(35);

        sampleRequest = new RestaurantRequest();
        sampleRequest.setName("Spice Garden");
        sampleRequest.setDescription("Authentic Indian cuisine");
        sampleRequest.setCuisineType("Indian");
        sampleRequest.setCity("Chennai");
        sampleRequest.setAddress("45 Anna Salai");
        sampleRequest.setPhone("+91-9876543210");
        sampleRequest.setEmail("spicegarden@swiftbite.com");
        sampleRequest.setDeliveryFee(new BigDecimal("30.00"));
        sampleRequest.setMinOrderAmount(new BigDecimal("150.00"));
        sampleRequest.setEstimatedDeliveryMinutes(35);
    }

    @Test
    void getAllRestaurants_noFilters_returnsAll() {
        when(restaurantRepository.findWithFilters(null, null, null))
                .thenReturn(List.of(sampleRestaurant));
        when(menuItemRepository.countByRestaurantId(1L)).thenReturn(6L);

        RestaurantSearchResponse result = restaurantService.getAllRestaurants(null, null, null);

        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRestaurants()).hasSize(1);
        assertThat(result.getRestaurants().get(0).getName()).isEqualTo("Spice Garden");
    }

    @Test
    void getAllRestaurants_withCityFilter_returnsFiltered() {
        when(restaurantRepository.findWithFilters(null, null, "Chennai"))
                .thenReturn(List.of(sampleRestaurant));
        when(menuItemRepository.countByRestaurantId(1L)).thenReturn(6L);

        RestaurantSearchResponse result = restaurantService.getAllRestaurants(null, null, "Chennai");

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRestaurants().get(0).getCity()).isEqualTo("Chennai");
    }

    @Test
    void getRestaurantById_found_returnsResponse() {
        when(restaurantRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(sampleRestaurant));
        when(menuItemRepository.countByRestaurantId(1L)).thenReturn(6L);

        RestaurantResponse response = restaurantService.getRestaurantById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Spice Garden");
        assertThat(response.getMenuItemCount()).isEqualTo(6);
    }

    @Test
    void getRestaurantById_notFound_throwsException() {
        when(restaurantRepository.findByIdAndActiveTrue(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.getRestaurantById(99L))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getActiveRestaurants_returnsOnlyActive() {
        when(restaurantRepository.findByActiveTrue()).thenReturn(List.of(sampleRestaurant));
        when(menuItemRepository.countByRestaurantId(1L)).thenReturn(6L);

        List<RestaurantResponse> responses = restaurantService.getActiveRestaurants();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).isActive()).isTrue();
    }

    @Test
    void createRestaurant_validRequest_returnsCreated() {
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(inv -> {
            Restaurant r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(menuItemRepository.countByRestaurantId(1L)).thenReturn(0L);

        RestaurantResponse response = restaurantService.createRestaurant(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Spice Garden");
        assertThat(response.getCuisineType()).isEqualTo("Indian");
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void updateRestaurant_found_returnsUpdated() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(sampleRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(sampleRestaurant);
        when(menuItemRepository.countByRestaurantId(1L)).thenReturn(6L);

        sampleRequest.setName("Spice Garden Updated");
        RestaurantResponse response = restaurantService.updateRestaurant(1L, sampleRequest);

        assertThat(response).isNotNull();
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void updateRestaurant_notFound_throwsException() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.updateRestaurant(99L, sampleRequest))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @Test
    void toggleRestaurantStatus_active_becomesInactive() {
        sampleRestaurant.setActive(true);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(sampleRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(menuItemRepository.countByRestaurantId(1L)).thenReturn(6L);

        RestaurantResponse response = restaurantService.toggleRestaurantStatus(1L);

        assertThat(response.isActive()).isFalse();
    }

    @Test
    void toggleRestaurantStatus_inactive_becomesActive() {
        sampleRestaurant.setActive(false);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(sampleRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(menuItemRepository.countByRestaurantId(1L)).thenReturn(6L);

        RestaurantResponse response = restaurantService.toggleRestaurantStatus(1L);

        assertThat(response.isActive()).isTrue();
    }

    @Test
    void searchRestaurants_withQuery_returnsResults() {
        when(restaurantRepository.searchActiveRestaurants("spice"))
                .thenReturn(List.of(sampleRestaurant));
        when(menuItemRepository.countByRestaurantId(1L)).thenReturn(6L);

        RestaurantSearchResponse result = restaurantService.searchRestaurants("spice");

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRestaurants().get(0).getName()).contains("Spice");
    }

    @Test
    void searchRestaurants_emptyQuery_returnsAll() {
        when(restaurantRepository.findByActiveTrue()).thenReturn(List.of(sampleRestaurant));
        when(menuItemRepository.countByRestaurantId(1L)).thenReturn(6L);

        RestaurantSearchResponse result = restaurantService.searchRestaurants("");

        assertThat(result).isNotNull();
        assertThat(result.getRestaurants()).hasSize(1);
    }
}
