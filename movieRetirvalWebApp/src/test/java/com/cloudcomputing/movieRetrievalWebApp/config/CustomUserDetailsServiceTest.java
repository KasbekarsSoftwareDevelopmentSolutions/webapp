package com.cloudcomputing.movieRetrievalWebApp.config;

import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private UserService userService;

    @Test
    void testLoadUserByUsernameValidUser() {
        // Arrange
        User mockUser = new User();
        mockUser.setEmailAddress("john.doe@example.com");
        mockUser.setPassword("encodedPassword");

        when(userService.getUserByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(mockUser));

        // Act
        org.springframework.security.core.userdetails.UserDetails userDetails =
                customUserDetailsService.loadUserByUsername("john.doe@example.com");

        // Assert
        assertNotNull(userDetails);
        Mockito.verify(userService).getUserByEmail("john.doe@example.com");
    }

    @Test
    void testLoadUserByUsernameInvalidUser() {
        // Arrange
        when(userService.getUserByEmail("invalid@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("invalid@example.com"));
    }
}
