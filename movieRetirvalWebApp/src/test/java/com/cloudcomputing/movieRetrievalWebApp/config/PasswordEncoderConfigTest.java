package com.cloudcomputing.movieRetrievalWebApp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PasswordEncoderConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Test password encoding
    @Test
    void testPasswordEncoding() {
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Ensure the password is encoded correctly (not equal to the raw password)
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    // Test password matching
    @Test
    void testPasswordMatching() {
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Ensure the encoded password matches the raw password
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }
}
