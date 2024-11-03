package com.cloudcomputing.movieRetrievalWebApp.controller;

import com.timgroup.statsd.StatsDClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

class HealthControllerUnitTest {

  @InjectMocks
  private HealthController healthController;

  @Mock
  private JdbcTemplate jdbcTemplate;

  @Mock
  private HttpServletRequest request;

  @Mock
  private StatsDClient statsDClient;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testHealthCheck_Success() {
    // Simulate a successful database query
    doNothing().when(jdbcTemplate).execute("SELECT 1"); // Change this to doNothing for void method

    ResponseEntity<Void> response = healthController.healthCheck(request);

    verify(statsDClient).incrementCounter("api.healthz.get.count");
    assertEquals(200, response.getStatusCodeValue()); // Check for OK status
  }

  @Test
  void testHealthCheck_BadRequest_ContentLength() {
    // Simulate request with content length greater than zero
    when(request.getContentLength()).thenReturn(1);

    ResponseEntity<Void> response = healthController.healthCheck(request);

    assertEquals(400, response.getStatusCodeValue());
  }

  @Test
  void testHealthCheck_BadRequest_QueryParameters() {
    // Simulate request with query parameters
    when(request.getParameterMap()).thenReturn(Map.of("param", new String[] { "value" }));

    ResponseEntity<Void> response = healthController.healthCheck(request);

    assertEquals(400, response.getStatusCodeValue());
  }

  @Test
  void testHealthCheck_ServiceUnavailable() {
    // Simulate database connectivity failure
    doThrow(new DataAccessException("DB connection error") {
    }).when(jdbcTemplate).execute("SELECT 1");

    ResponseEntity<Void> response = healthController.healthCheck(request);

    assertEquals(503, response.getStatusCodeValue()); // Check for SERVICE UNAVAILABLE status
  }

  @Test
  void testMethodNotAllowed() {
    // Simulating request using POST or other methods can be done here
    ResponseEntity<Void> response = healthController.methodNotAllowed();

    assertEquals(405, response.getStatusCodeValue());
  }
}
