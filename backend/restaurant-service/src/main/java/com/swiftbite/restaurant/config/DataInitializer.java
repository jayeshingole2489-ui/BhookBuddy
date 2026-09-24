package com.swiftbite.restaurant.config;

import com.swiftbite.restaurant.entity.Category;
import com.swiftbite.restaurant.entity.MenuItem;
import com.swiftbite.restaurant.entity.Restaurant;
import com.swiftbite.restaurant.repository.CategoryRepository;
import com.swiftbite.restaurant.repository.RestaurantRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer {

    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;

    public DataInitializer(RestaurantRepository restaurantRepository,
                           CategoryRepository categoryRepository) {
        this.restaurantRepository = restaurantRepository;
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    @Transactional
    public void init() {
        initCategories();
        if (restaurantRepository.count() == 0) {
            initRestaurants();
        }
    }

    private void initCategories() {
        List<Object[]> categoryData = List.of(
                new Object[]{"Indian", "🍛"},
                new Object[]{"Chinese", "🍜"},
                new Object[]{"Italian", "🍝"},
                new Object[]{"American", "🍔"},
                new Object[]{"Mexican", "🌮"},
                new Object[]{"Pizza", "🍕"},
                new Object[]{"Biryani", "🍚"},
                new Object[]{"Burgers", "🍔"},
                new Object[]{"Desserts", "🍰"},
                new Object[]{"Beverages", "☕"}
        );

        for (Object[] data : categoryData) {
            String name = (String) data[0];
            String icon = (String) data[1];
            if (!categoryRepository.existsByNameIgnoreCase(name)) {
                categoryRepository.save(new Category(name, icon));
            }
        }
    }

    private void initRestaurants() {
        // Restaurant 1: Spice Garden
        Restaurant spiceGarden = new Restaurant();
        spiceGarden.setName("Spice Garden");
        spiceGarden.setDescription("Authentic Indian cuisine with aromatic spices and traditional recipes");
        spiceGarden.setCuisineType("Indian");
        spiceGarden.setAddress("45 Anna Salai, Teynampet, Chennai");
        spiceGarden.setCity("Chennai");
        spiceGarden.setPhone("+91-9876543210");
        spiceGarden.setEmail("spicegarden@swiftbite.com");
        spiceGarden.setRating(new BigDecimal("4.3"));
        spiceGarden.setDeliveryFee(new BigDecimal("30.00"));
        spiceGarden.setMinOrderAmount(new BigDecimal("150.00"));
        spiceGarden.setEstimatedDeliveryMinutes(35);

        spiceGarden.getMenuItems().addAll(List.of(
                buildItem(spiceGarden, "Butter Chicken", "Tender chicken in rich buttery tomato gravy", new BigDecimal("280.00"), "Main Course", false),
                buildItem(spiceGarden, "Paneer Tikka Masala", "Grilled paneer in spiced tomato onion gravy", new BigDecimal("250.00"), "Main Course", true),
                buildItem(spiceGarden, "Dal Makhani", "Slow-cooked black lentils with cream and spices", new BigDecimal("200.00"), "Main Course", true),
                buildItem(spiceGarden, "Garlic Naan", "Soft leavened bread topped with garlic and butter", new BigDecimal("40.00"), "Bread", true),
                buildItem(spiceGarden, "Gulab Jamun", "Soft milk-solid dumplings soaked in rose syrup", new BigDecimal("80.00"), "Dessert", true),
                buildItem(spiceGarden, "Mango Lassi", "Chilled yoghurt drink blended with fresh mango", new BigDecimal("60.00"), "Beverage", true)
        ));
        restaurantRepository.save(spiceGarden);

        // Restaurant 2: Dragon Palace
        Restaurant dragonPalace = new Restaurant();
        dragonPalace.setName("Dragon Palace");
        dragonPalace.setDescription("Authentic Chinese flavours with wok-tossed goodness");
        dragonPalace.setCuisineType("Chinese");
        dragonPalace.setAddress("12 Nungambakkam High Road, Chennai");
        dragonPalace.setCity("Chennai");
        dragonPalace.setPhone("+91-9876543211");
        dragonPalace.setEmail("dragonpalace@swiftbite.com");
        dragonPalace.setRating(new BigDecimal("4.1"));
        dragonPalace.setDeliveryFee(new BigDecimal("35.00"));
        dragonPalace.setMinOrderAmount(new BigDecimal("200.00"));
        dragonPalace.setEstimatedDeliveryMinutes(40);

        dragonPalace.getMenuItems().addAll(List.of(
                buildItem(dragonPalace, "Veg Fried Rice", "Wok-tossed rice with fresh vegetables and soy", new BigDecimal("180.00"), "Main Course", true),
                buildItem(dragonPalace, "Chicken Fried Rice", "Wok-tossed rice with chicken strips and soy sauce", new BigDecimal("220.00"), "Main Course", false),
                buildItem(dragonPalace, "Veg Manchurian", "Fried vegetable balls in spicy Manchurian sauce", new BigDecimal("160.00"), "Starter", true),
                buildItem(dragonPalace, "Chicken Manchurian", "Crispy chicken in savoury Manchurian gravy", new BigDecimal("200.00"), "Starter", false),
                buildItem(dragonPalace, "Hot & Sour Soup", "Classic Chinese soup with vegetables and vinegar", new BigDecimal("120.00"), "Soup", true),
                buildItem(dragonPalace, "Spring Rolls", "Crispy rolls stuffed with seasoned vegetables", new BigDecimal("140.00"), "Starter", true)
        ));
        restaurantRepository.save(dragonPalace);

        // Restaurant 3: Pizza Parlour
        Restaurant pizzaParlour = new Restaurant();
        pizzaParlour.setName("Pizza Parlour");
        pizzaParlour.setDescription("Wood-fired Italian pizzas and fresh pastas crafted with love");
        pizzaParlour.setCuisineType("Italian");
        pizzaParlour.setAddress("88 Greams Road, Thousand Lights, Chennai");
        pizzaParlour.setCity("Chennai");
        pizzaParlour.setPhone("+91-9876543212");
        pizzaParlour.setEmail("pizzaparlour@swiftbite.com");
        pizzaParlour.setRating(new BigDecimal("4.4"));
        pizzaParlour.setDeliveryFee(new BigDecimal("40.00"));
        pizzaParlour.setMinOrderAmount(new BigDecimal("250.00"));
        pizzaParlour.setEstimatedDeliveryMinutes(45);

        pizzaParlour.getMenuItems().addAll(List.of(
                buildItem(pizzaParlour, "Margherita Pizza", "Classic tomato sauce, fresh mozzarella and basil", new BigDecimal("280.00"), "Pizza", true),
                buildItem(pizzaParlour, "Pepperoni Pizza", "Loaded with premium pepperoni and melted cheese", new BigDecimal("350.00"), "Pizza", false),
                buildItem(pizzaParlour, "Pasta Arrabiata", "Penne in fiery tomato and garlic sauce", new BigDecimal("220.00"), "Pasta", true),
                buildItem(pizzaParlour, "Garlic Bread", "Toasted bread with garlic butter and herbs", new BigDecimal("100.00"), "Starter", true),
                buildItem(pizzaParlour, "Tiramisu", "Italian coffee dessert with mascarpone cream", new BigDecimal("160.00"), "Dessert", true),
                buildItem(pizzaParlour, "Coke", "Chilled Coca-Cola 330ml", new BigDecimal("60.00"), "Beverage", true)
        ));
        restaurantRepository.save(pizzaParlour);

        // Restaurant 4: Burger Hub
        Restaurant burgerHub = new Restaurant();
        burgerHub.setName("Burger Hub");
        burgerHub.setDescription("Juicy American-style burgers stacked high with fresh toppings");
        burgerHub.setCuisineType("American");
        burgerHub.setAddress("22 Cathedral Road, Gopalapuram, Chennai");
        burgerHub.setCity("Chennai");
        burgerHub.setPhone("+91-9876543213");
        burgerHub.setEmail("burgerhub@swiftbite.com");
        burgerHub.setRating(new BigDecimal("4.2"));
        burgerHub.setDeliveryFee(new BigDecimal("25.00"));
        burgerHub.setMinOrderAmount(new BigDecimal("100.00"));
        burgerHub.setEstimatedDeliveryMinutes(25);

        burgerHub.getMenuItems().addAll(List.of(
                buildItem(burgerHub, "Classic Veg Burger", "Crispy veggie patty with fresh lettuce and sauce", new BigDecimal("180.00"), "Burger", true),
                buildItem(burgerHub, "Chicken Zinger", "Spicy crispy chicken fillet in a toasted bun", new BigDecimal("220.00"), "Burger", false),
                buildItem(burgerHub, "Loaded Fries", "Golden fries topped with cheese sauce and jalapenos", new BigDecimal("120.00"), "Sides", true),
                buildItem(burgerHub, "Onion Rings", "Crispy battered onion rings with dipping sauce", new BigDecimal("100.00"), "Sides", true),
                buildItem(burgerHub, "Chocolate Shake", "Thick and creamy chocolate milkshake", new BigDecimal("150.00"), "Beverage", true),
                buildItem(burgerHub, "Lemonade", "Freshly squeezed lemonade with a hint of mint", new BigDecimal("80.00"), "Beverage", true)
        ));
        restaurantRepository.save(burgerHub);

        // Restaurant 5: Biryani House
        Restaurant biryaniHouse = new Restaurant();
        biryaniHouse.setName("Biryani House");
        biryaniHouse.setDescription("Dum-cooked biryani with fragrant basmati rice and whole spices");
        biryaniHouse.setCuisineType("Indian");
        biryaniHouse.setAddress("67 Luz Avenue, Mylapore, Chennai");
        biryaniHouse.setCity("Chennai");
        biryaniHouse.setPhone("+91-9876543214");
        biryaniHouse.setEmail("biryanihouse@swiftbite.com");
        biryaniHouse.setRating(new BigDecimal("4.5"));
        biryaniHouse.setDeliveryFee(new BigDecimal("30.00"));
        biryaniHouse.setMinOrderAmount(new BigDecimal("200.00"));
        biryaniHouse.setEstimatedDeliveryMinutes(40);

        biryaniHouse.getMenuItems().addAll(List.of(
                buildItem(biryaniHouse, "Chicken Biryani", "Fragrant basmati rice layered with spiced chicken", new BigDecimal("320.00"), "Main Course", false),
                buildItem(biryaniHouse, "Mutton Biryani", "Slow-cooked mutton dum biryani with caramelised onions", new BigDecimal("380.00"), "Main Course", false),
                buildItem(biryaniHouse, "Veg Biryani", "Aromatic basmati with seasonal vegetables and saffron", new BigDecimal("240.00"), "Main Course", true),
                buildItem(biryaniHouse, "Chicken Curry", "Rich tomato-based chicken curry", new BigDecimal("200.00"), "Side", false),
                buildItem(biryaniHouse, "Raita", "Chilled yoghurt with cucumber and cumin", new BigDecimal("60.00"), "Side", true),
                buildItem(biryaniHouse, "Double Ka Meetha", "Hyderabadi bread pudding with rabri and nuts", new BigDecimal("100.00"), "Dessert", true)
        ));
        restaurantRepository.save(biryaniHouse);
    }

    private MenuItem buildItem(Restaurant restaurant, String name, String description,
                               BigDecimal price, String category, boolean vegetarian) {
        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setVegetarian(vegetarian);
        item.setAvailable(true);
        return item;
    }
}
