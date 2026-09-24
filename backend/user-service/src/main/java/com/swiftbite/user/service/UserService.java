package com.swiftbite.user.service;

import com.swiftbite.user.dto.request.AddressRequest;
import com.swiftbite.user.dto.request.UpdateProfileRequest;
import com.swiftbite.user.dto.response.AddressResponse;
import com.swiftbite.user.dto.response.UserResponse;
import com.swiftbite.user.entity.Address;
import com.swiftbite.user.entity.User;
import com.swiftbite.user.exception.AddressNotFoundException;
import com.swiftbite.user.exception.UserNotFoundException;
import com.swiftbite.user.repository.AddressRepository;
import com.swiftbite.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserService(UserRepository userRepository, AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return mapToUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        User updated = userRepository.save(user);
        return mapToUserResponse(updated);
    }

    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setActive(active);
        User updated = userRepository.save(user);
        return mapToUserResponse(updated);
    }

    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Address address = new Address();
        address.setUser(user);
        address.setLabel(request.getLabel());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry() != null ? request.getCountry() : "India");
        address.setDefault(request.isDefault());

        Address saved = addressRepository.save(address);
        return mapToAddressResponse(saved);
    }

    public List<AddressResponse> getUserAddresses(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return addressRepository.findByUserId(userId)
                .stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AddressNotFoundException(addressId, userId));

        if (request.getLabel() != null) {
            address.setLabel(request.getLabel());
        }
        if (request.getStreet() != null && !request.getStreet().isBlank()) {
            address.setStreet(request.getStreet());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null && !request.getState().isBlank()) {
            address.setState(request.getState());
        }
        if (request.getZipCode() != null && !request.getZipCode().isBlank()) {
            address.setZipCode(request.getZipCode());
        }
        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            address.setCountry(request.getCountry());
        }
        address.setDefault(request.isDefault());

        Address updated = addressRepository.save(address);
        return mapToAddressResponse(updated);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AddressNotFoundException(addressId, userId));
        addressRepository.delete(address);
    }

    // ---- Mappers ----

    public UserResponse mapToUserResponse(User user) {
        List<AddressResponse> addressResponses = user.getAddresses()
                .stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                addressResponses
        );
    }

    public AddressResponse mapToAddressResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getLabel(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry(),
                address.isDefault()
        );
    }
}
