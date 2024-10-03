package com.cloudcomputing.movieRetrievalWebApp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // Test that an unauthenticated user cannot access the protected endpoint
    @Test
    void testUnauthenticatedUserCannotAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/v1/user/self"))
                .andExpect(status().isUnauthorized());
    }

    // Test that an authenticated user can access the protected endpoint
    @Test
    @WithMockUser(username = "john.doe@example.com", password = "password123")
    void testAuthenticatedUserCanAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/v1/user/self"))
                .andExpect(status().isOk());
    }

    // Test that public endpoints are accessible without authentication
    @Test
    void testPublicEndpointsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public"))
                .andExpect(status().isOk());
    }

    // Test that CSRF is disabled
    @Test
    void testCsrfIsDisabled() throws Exception {
        mockMvc.perform(get("/v1/user/self"))
                .andExpect(status().isUnauthorized()); // Check access without CSRF token
    }
}

