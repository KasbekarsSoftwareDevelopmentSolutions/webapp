package com.cloudcomputing.movieRetrievalWebApp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
public class HealthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Reset any previous interactions with the JdbcTemplate mock.
        Mockito.reset(jdbcTemplate);
    }

    @Test
    void testHealthCheck_Success() throws Exception {
        // Simulate a successful DB connection
        mockMvc.perform(MockMvcRequestBuilders.get("/healthz"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "no-sniff"));
    }

    @Test
    void testHealthCheck_BadRequest_ContentLength() throws Exception {
        // Test bad request when content length is greater than zero
        mockMvc.perform(MockMvcRequestBuilders.get("/healthz")
                        .content("Invalid content")) // non-empty content
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "no-sniff"));
    }

    @Test
    void testHealthCheck_BadRequest_QueryParameters() throws Exception {
        // Test bad request when query parameters are present
        mockMvc.perform(MockMvcRequestBuilders.get("/healthz")
                        .param("param", "value"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "no-sniff"));
    }

    @Test
    void testHealthCheck_ServiceUnavailable() throws Exception {
        // Simulate database connection failure
        doThrow(new DataAccessException("DB connection error") {}).when(jdbcTemplate).execute("SELECT 1");

        mockMvc.perform(MockMvcRequestBuilders.get("/healthz"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "no-sniff"));
    }

    @Test
    void testMethodNotAllowed_Post() throws Exception {
        // Test unsupported HTTP method POST
        mockMvc.perform(MockMvcRequestBuilders.post("/healthz"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "no-sniff"));
    }

    @Test
    void testMethodNotAllowed_Put() throws Exception {
        // Test unsupported HTTP method PUT
        mockMvc.perform(MockMvcRequestBuilders.put("/healthz"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "no-sniff"));
    }

    @Test
    void testMethodNotAllowed_Delete() throws Exception {
        // Test unsupported HTTP method DELETE
        mockMvc.perform(MockMvcRequestBuilders.delete("/healthz"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "no-sniff"));
    }

    @Test
    void testMethodNotAllowed_Patch() throws Exception {
        // Test unsupported HTTP method PATCH
        mockMvc.perform(MockMvcRequestBuilders.patch("/healthz"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "no-sniff"));
    }
}
