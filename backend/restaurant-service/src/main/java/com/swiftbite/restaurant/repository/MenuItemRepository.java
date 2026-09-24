package com.swiftbite.restaurant.repository;

import com.swiftbite.restaurant.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndCategoryIgnoreCase(Long restaurantId, String category);

    List<MenuItem> findByRestaurantIdAndAvailableTrueAndCategoryIgnoreCase(Long restaurantId, String category);

    List<MenuItem> findByIdIn(List<Long> ids);

    long countByRestaurantId(Long restaurantId);
}
