package com.cloudcomputing.movieRetrievalWebApp.controller;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

@WebMvcTest(MovieController.class)
public class MovieControllerTest {

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @MockBean
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @BeforeEach
  void setUp() {
    // Reset any previous interactions with the JdbcTemplate mock.
    Mockito.reset(jdbcTemplate);
  }

  // Method source for various HTTP methods
  private static Stream<Arguments> httpMethods() {
    return Stream.of(
        Arguments.of(MockMvcRequestBuilders.get("/")),
        Arguments.of(MockMvcRequestBuilders.post("/")),
        Arguments.of(MockMvcRequestBuilders.put("/")),
        Arguments.of(MockMvcRequestBuilders.delete("/")),
        Arguments.of(MockMvcRequestBuilders.patch("/")));
  }

  @ParameterizedTest
  @MethodSource("httpMethods")
  @WithAnonymousUser
  @DisplayName("Test resource not available for various HTTP methods")
  void testResourceNotAvailable(MockHttpServletRequestBuilder requestBuilder) throws Exception {
    mockMvc.perform(requestBuilder)
        .andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void testPostEmptyBody() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void testPutEmptyBody() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put("/")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isNotFound());
  }

}
