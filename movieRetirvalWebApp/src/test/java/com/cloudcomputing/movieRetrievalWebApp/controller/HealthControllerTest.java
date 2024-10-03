package com.cloudcomputing.movieRetrievalWebApp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class HealthControllerTest {

    @InjectMocks
    private HealthController healthController;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test 1: Health Check - Successful
    @Test
    void testHealthCheck_Success() {
        when(request.getContentLength()).thenReturn(0);
        doNothing().when(jdbcTemplate).execute("SELECT 1");

        ResponseEntity<Void> response = healthController.healthCheck(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("Cache-Control"));
        assertEquals("no-cache", response.getHeaders().getFirst("Cache-Control"));
    }

    // Test 2: Health Check - DataAccessException (Service Unavailable)
    @Test
    void testHealthCheck_DataAccessException() {
        when(request.getContentLength()).thenReturn(0);
        doThrow(new DataAccessException("Database error") {}).when(jdbcTemplate).execute("SELECT 1");

        ResponseEntity<Void> response = healthController.healthCheck(request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("Cache-Control"));
        assertEquals("no-cache", response.getHeaders().getFirst("Cache-Control"));
    }

    // Test 3: Health Check - Bad Request due to Content-Length > 0
    @Test
    void testHealthCheck_BadRequest() {
        when(request.getContentLength()).thenReturn(10); // Simulate a non-empty request

        ResponseEntity<Void> response = healthController.healthCheck(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("Cache-Control"));
        assertEquals("no-cache", response.getHeaders().getFirst("Cache-Control"));
    }

    // Test 4: Method Not Allowed (POST/PUT/DELETE/PATCH)
    @Test
    void testMethodNotAllowed() {
        ResponseEntity<Void> response = healthController.methodNotAllowed();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("Cache-Control"));
        assertEquals("no-cache", response.getHeaders().getFirst("Cache-Control"));
    }
}
