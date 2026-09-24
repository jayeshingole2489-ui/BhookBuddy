package com.swiftbite.user.config;

import com.swiftbite.user.entity.Address;
import com.swiftbite.user.entity.Role;
import com.swiftbite.user.entity.User;
import com.swiftbite.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initData() {
        createAdminUser();
        createDemoUser();
    }

    private void createAdminUser() {
        if (userRepository.existsByEmail("admin@swiftbite.com")) {
            return;
        }

        User admin = new User();
        admin.setEmail("admin@swiftbite.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setFirstName("Admin");
        admin.setLastName("SwiftBite");
        admin.setPhone("+911234567890");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setActive(true);

        userRepository.save(admin);
    }

    private void createDemoUser() {
        if (userRepository.existsByEmail("john@example.com")) {
            return;
        }

        User john = new User();
        john.setEmail("john@example.com");
        john.setPassword(passwordEncoder.encode("User@123"));
        john.setFirstName("John");
        john.setLastName("Doe");
        john.setPhone("+919876543210");
        john.setRole(Role.ROLE_USER);
        john.setActive(true);

        Address homeAddress = new Address();
        homeAddress.setUser(john);
        homeAddress.setLabel("Home");
        homeAddress.setStreet("123 Main St");
        homeAddress.setCity("Adyar");
        homeAddress.setState("Tamil Nadu");
        homeAddress.setZipCode("600020");
        homeAddress.setCountry("India");
        homeAddress.setDefault(true);

        john.getAddresses().add(homeAddress);

        userRepository.save(john);
    }
}
