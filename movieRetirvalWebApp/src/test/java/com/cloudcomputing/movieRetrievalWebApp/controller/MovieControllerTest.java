package com.cloudcomputing.movieRetrievalWebApp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class MovieControllerTest {

    @InjectMocks
    private MovieController movieController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test 1: GET request should return 404 Not Found
    @Test
    void testResourceNotAvailable_GET() {
        ResponseEntity<Void> response = movieController.resourceNotAvailable();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Test 2: POST request should return 404 Not Found
    @Test
    void testResourceNotAvailable_POST() {
        ResponseEntity<Void> response = movieController.resourceNotAvailable();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Test 3: PUT request should return 404 Not Found
    @Test
    void testResourceNotAvailable_PUT() {
        ResponseEntity<Void> response = movieController.resourceNotAvailable();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Test 4: DELETE request should return 404 Not Found
    @Test
    void testResourceNotAvailable_DELETE() {
        ResponseEntity<Void> response = movieController.resourceNotAvailable();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Test 5: PATCH request should return 404 Not Found
    @Test
    void testResourceNotAvailable_PATCH() {
        ResponseEntity<Void> response = movieController.resourceNotAvailable();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}