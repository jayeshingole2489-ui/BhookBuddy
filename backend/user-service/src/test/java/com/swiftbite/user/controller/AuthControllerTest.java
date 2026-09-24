package com.swiftbite.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftbite.user.config.PasswordEncoderConfig;
import com.swiftbite.user.config.SecurityConfig;
import com.swiftbite.user.dto.request.LoginRequest;
import com.swiftbite.user.dto.request.RegisterRequest;
import com.swiftbite.user.dto.response.AuthResponse;
import com.swiftbite.user.entity.Role;
import com.swiftbite.user.exception.EmailAlreadyExistsException;
import com.swiftbite.user.exception.GlobalExceptionHandler;
import com.swiftbite.user.filter.JwtAuthenticationFilter;
import com.swiftbite.user.service.AuthService;
import com.swiftbite.user.service.JwtService;
import com.swiftbite.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    private AuthResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        mockAuthResponse = new AuthResponse(
                "mock.jwt.token",
                1L,
                "test@example.com",
                "Test",
                "User",
                Role.ROLE_USER
        );
    }

    @Test
    @DisplayName("POST /api/auth/register - success returns 201 with token")
    void register_success_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "Password@123",
                "Test",
                "User",
                "+911234567890"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("POST /api/auth/register - duplicate email returns 409")
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "existing@example.com",
                "Password@123",
                "Test",
                "User",
                null
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("existing@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already registered: existing@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register - invalid payload returns 400")
    void register_invalidPayload_returns400() throws Exception {
        // Missing required fields — empty email and password
        RegisterRequest request = new RegisterRequest("", "", "T", "U", null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/auth/login - success returns 200 with token")
    void login_success_returns200() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "Password@123");

        when(authService.login(any(LoginRequest.class))).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - bad credentials returns 401")
    void login_badCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "WrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/auth/login - missing email returns 400")
    void login_missingEmail_returns400() throws Exception {
        LoginRequest request = new LoginRequest("", "Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
