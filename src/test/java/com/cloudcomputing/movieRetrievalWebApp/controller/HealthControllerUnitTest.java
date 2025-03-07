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

    doNothing().when(jdbcTemplate).execute("SELECT 1");

    ResponseEntity<Void> response = healthController.healthCheck(request);

    verify(statsDClient).incrementCounter("api.healthz.get.count");
    assertEquals(200, response.getStatusCodeValue());
  }

  @Test
  void testHealthCheck_BadRequest_ContentLength() {

    when(request.getContentLength()).thenReturn(1);

    ResponseEntity<Void> response = healthController.healthCheck(request);

    assertEquals(400, response.getStatusCodeValue());
  }

  @Test
  void testHealthCheck_BadRequest_QueryParameters() {

    when(request.getParameterMap()).thenReturn(Map.of("param", new String[] { "value" }));

    ResponseEntity<Void> response = healthController.healthCheck(request);

    assertEquals(400, response.getStatusCodeValue());
  }

  @Test
  void testHealthCheck_ServiceUnavailable() {

    doThrow(new DataAccessException("DB connection error") {
    }).when(jdbcTemplate).execute("SELECT 1");

    ResponseEntity<Void> response = healthController.healthCheck(request);

    assertEquals(503, response.getStatusCodeValue());
  }

  @Test
  void testMethodNotAllowed() {

    ResponseEntity<Void> response = healthController.methodNotAllowed();

    assertEquals(405, response.getStatusCodeValue());
  }
}
