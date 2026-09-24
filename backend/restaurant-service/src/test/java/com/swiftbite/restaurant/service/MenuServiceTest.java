package com.swiftbite.restaurant.service;

import com.swiftbite.restaurant.dto.MenuItemRequest;
import com.swiftbite.restaurant.dto.MenuItemResponse;
import com.swiftbite.restaurant.entity.MenuItem;
import com.swiftbite.restaurant.entity.Restaurant;
import com.swiftbite.restaurant.exception.MenuItemNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private MenuService menuService;

    private Restaurant sampleRestaurant;
    private MenuItem sampleItem;
    private MenuItemRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRestaurant = new Restaurant();
        sampleRestaurant.setId(1L);
        sampleRestaurant.setName("Spice Garden");
        sampleRestaurant.setCuisineType("Indian");
        sampleRestaurant.setCity("Chennai");
        sampleRestaurant.setActive(true);
        sampleRestaurant.setDeliveryFee(new BigDecimal("30.00"));
        sampleRestaurant.setMinOrderAmount(new BigDecimal("150.00"));
        sampleRestaurant.setEstimatedDeliveryMinutes(35);

        sampleItem = new MenuItem();
        sampleItem.setId(10L);
        sampleItem.setRestaurant(sampleRestaurant);
        sampleItem.setName("Butter Chicken");
        sampleItem.setDescription("Tender chicken in buttery tomato gravy");
        sampleItem.setPrice(new BigDecimal("280.00"));
        sampleItem.setCategory("Main Course");
        sampleItem.setAvailable(true);
        sampleItem.setVegetarian(false);

        sampleRequest = new MenuItemRequest();
        sampleRequest.setName("Butter Chicken");
        sampleRequest.setDescription("Tender chicken in buttery tomato gravy");
        sampleRequest.setPrice(new BigDecimal("280.00"));
        sampleRequest.setCategory("Main Course");
        sampleRequest.setVegetarian(false);
    }

    @Test
    void getMenuByRestaurant_restaurantExists_returnsItems() {
        when(restaurantRepository.existsById(1L)).thenReturn(true);
        when(menuItemRepository.findByRestaurantId(1L)).thenReturn(List.of(sampleItem));

        List<MenuItemResponse> result = menuService.getMenuByRestaurant(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Butter Chicken");
        assertThat(result.get(0).getRestaurantId()).isEqualTo(1L);
        assertThat(result.get(0).getRestaurantName()).isEqualTo("Spice Garden");
    }

    @Test
    void getMenuByRestaurant_restaurantNotFound_throwsException() {
        when(restaurantRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> menuService.getMenuByRestaurant(99L))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getMenuItemById_found_returnsItem() {
        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(sampleItem));

        MenuItemResponse response = menuService.getMenuItemById(10L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Butter Chicken");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("280.00"));
    }

    @Test
    void getMenuItemById_notFound_throwsException() {
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getMenuItemById(999L))
                .isInstanceOf(MenuItemNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void addMenuItem_validRequest_returnsCreated() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(sampleRestaurant));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(inv -> {
            MenuItem item = inv.getArgument(0);
            item.setId(10L);
            return item;
        });

        MenuItemResponse response = menuService.addMenuItem(1L, sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Butter Chicken");
        assertThat(response.getRestaurantId()).isEqualTo(1L);
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("280.00"));
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    void addMenuItem_restaurantNotFound_throwsException() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.addMenuItem(99L, sampleRequest))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @Test
    void updateMenuItem_found_returnsUpdated() {
        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(sampleItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(sampleItem);

        sampleRequest.setName("Butter Chicken Special");
        sampleRequest.setPrice(new BigDecimal("310.00"));

        MenuItemResponse response = menuService.updateMenuItem(10L, sampleRequest);

        assertThat(response).isNotNull();
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void updateMenuItem_notFound_throwsException() {
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.updateMenuItem(999L, sampleRequest))
                .isInstanceOf(MenuItemNotFoundException.class);
    }

    @Test
    void toggleItemAvailability_available_becomesUnavailable() {
        sampleItem.setAvailable(true);
        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(sampleItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItemResponse response = menuService.toggleItemAvailability(10L);

        assertThat(response.isAvailable()).isFalse();
    }

    @Test
    void toggleItemAvailability_unavailable_becomesAvailable() {
        sampleItem.setAvailable(false);
        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(sampleItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItemResponse response = menuService.toggleItemAvailability(10L);

        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    void deleteMenuItem_found_deletesSuccessfully() {
        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(sampleItem));
        doNothing().when(menuItemRepository).delete(any(MenuItem.class));

        menuService.deleteMenuItem(10L);

        verify(menuItemRepository, times(1)).delete(sampleItem);
    }

    @Test
    void deleteMenuItem_notFound_throwsException() {
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.deleteMenuItem(999L))
                .isInstanceOf(MenuItemNotFoundException.class);
    }

    @Test
    void getMenuItemsByIds_returnsMatchingItems() {
        when(menuItemRepository.findByIdIn(List.of(10L))).thenReturn(List.of(sampleItem));

        List<MenuItemResponse> result = menuService.getMenuItemsByIds(List.of(10L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
    }

    @Test
    void getMenuItemsByCategory_returnsItemsInCategory() {
        when(restaurantRepository.existsById(1L)).thenReturn(true);
        when(menuItemRepository.findByRestaurantIdAndCategoryIgnoreCase(1L, "Main Course"))
                .thenReturn(List.of(sampleItem));

        List<MenuItemResponse> result = menuService.getMenuItemsByCategory(1L, "Main Course");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Main Course");
    }
}
