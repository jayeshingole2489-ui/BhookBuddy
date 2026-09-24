package com.swiftbite.user.service;

import com.swiftbite.user.dto.request.LoginRequest;
import com.swiftbite.user.dto.request.RegisterRequest;
import com.swiftbite.user.dto.response.AuthResponse;
import com.swiftbite.user.entity.Role;
import com.swiftbite.user.entity.User;
import com.swiftbite.user.exception.EmailAlreadyExistsException;
import com.swiftbite.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "newuser@example.com",
                "Password@123",
                "New",
                "User",
                "+911234567890"
        );

        loginRequest = new LoginRequest("john@example.com", "User@123");

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("newuser@example.com");
        savedUser.setPassword("$2a$10$hashedpassword");
        savedUser.setFirstName("New");
        savedUser.setLastName("User");
        savedUser.setRole(Role.ROLE_USER);
        savedUser.setActive(true);
    }

    @Test
    @DisplayName("Register - success creates user and returns token")
    void register_success() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
        assertThat(response.getFirstName()).isEqualTo("New");
        assertThat(response.getLastName()).isEqualTo("User");
        assertThat(response.getRole()).isEqualTo(Role.ROLE_USER);

        verify(userRepository).existsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("Password@123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Register - duplicate email throws EmailAlreadyExistsException")
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("newuser@example.com");

        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Login - success returns auth response with token")
    void login_success() {
        User johnUser = new User();
        johnUser.setId(2L);
        johnUser.setEmail("john@example.com");
        johnUser.setPassword("$2a$10$hashedUserPass");
        johnUser.setFirstName("John");
        johnUser.setLastName("Doe");
        johnUser.setRole(Role.ROLE_USER);
        johnUser.setActive(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("john@example.com", null));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(johnUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("mock.login.token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock.login.token");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getRole()).isEqualTo(Role.ROLE_USER);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("john@example.com");
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Login - bad credentials throws BadCredentialsException")
    void login_badCredentials_throwsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("LoadUserByUsername - returns UserDetails for known email")
    void loadUserByUsername_success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(savedUser));

        var userDetails = authService.loadUserByUsername("john@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("newuser@example.com");
    }

    @Test
    @DisplayName("LoadUserByUsername - throws UsernameNotFoundException for unknown email")
    void loadUserByUsername_notFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(org.springframework.security.core.userdetails.UsernameNotFoundException.class);
    }
}
