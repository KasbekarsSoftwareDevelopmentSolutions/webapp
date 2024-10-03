package com.cloudcomputing.movieRetrievalWebApp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/healthz")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<Void> healthCheck(HttpServletRequest request) {
        // Check for query parameters
        Map<String, String[]> queryParams = request.getParameterMap();
        if (request.getContentLength() > 0 || !queryParams.isEmpty()) {
            return ResponseEntity.badRequest()
                    .header("Cache-Control", "no-cache", "no-store", "must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "no-sniff")
                    .build();
        }

        try {
            jdbcTemplate.execute("SELECT 1");
            return ResponseEntity.ok()
                    .header("Cache-Control", "no-cache", "no-store", "must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "no-sniff")
                    .build();
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header("Cache-Control", "no-cache", "no-store", "must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "no-sniff")
                    .build();
        }
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<Void> methodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header("Cache-Control", "no-cache", "no-store", "must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "no-sniff")
                .build();
    }
}
