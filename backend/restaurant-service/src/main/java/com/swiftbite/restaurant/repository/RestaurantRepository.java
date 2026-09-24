package com.swiftbite.restaurant.repository;

import com.swiftbite.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByActiveTrue();

    List<Restaurant> findByCityIgnoreCase(String city);

    List<Restaurant> findByCuisineTypeIgnoreCase(String cuisineType);

    List<Restaurant> findByActiveTrueAndCityIgnoreCase(String city);

    List<Restaurant> findByActiveTrueAndCuisineTypeIgnoreCase(String cuisineType);

    List<Restaurant> findByActiveTrueAndCityIgnoreCaseAndCuisineTypeIgnoreCase(String city, String cuisineType);

    @Query("SELECT r FROM Restaurant r WHERE r.active = true AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.city) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Restaurant> searchActiveRestaurants(@Param("query") String query);

    @Query("SELECT r FROM Restaurant r WHERE " +
           "(:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.city) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:cuisineType IS NULL OR LOWER(r.cuisineType) = LOWER(:cuisineType)) AND " +
           "(:city IS NULL OR LOWER(r.city) = LOWER(:city))")
    List<Restaurant> findWithFilters(
            @Param("search") String search,
            @Param("cuisineType") String cuisineType,
            @Param("city") String city);

    Optional<Restaurant> findByIdAndActiveTrue(Long id);

    boolean existsByName(String name);
}
