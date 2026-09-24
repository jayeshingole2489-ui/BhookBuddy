package com.swiftbite.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "jwt.secret=swiftbite-test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context starts without errors.
    }
}
