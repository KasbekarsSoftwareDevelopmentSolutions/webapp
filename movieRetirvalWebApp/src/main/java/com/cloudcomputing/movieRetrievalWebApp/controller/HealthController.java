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
import java.util.logging.Logger;

@RestController
@RequestMapping("/healthz")
public class HealthController {
    private static final Logger LOGGER = Logger.getLogger(HealthController.class.getName());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<Void> healthCheck(HttpServletRequest request) {
        LOGGER.info("Health check endpoint accessed.");
        Map<String, String[]> queryParams = request.getParameterMap();

        if (request.getContentLength() > 0 || !queryParams.isEmpty()) {
            LOGGER.warning("Invalid request: Content length is greater than zero or query parameters are present." + " ##HttpStatus.BAD_REQUEST sent in response## ");
            return ResponseEntity.badRequest()
                    .header("Cache-Control", "no-cache", "no-store", "must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "no-sniff")
                    .build();
        }

        try {
            LOGGER.info("Executing a simple database query to check connectivity.");
            jdbcTemplate.execute("SELECT 1");
            LOGGER.info("Database connectivity check successful." + " ##HttpStatus.OK sent in response## ");
            return ResponseEntity.ok()
                    .header("Cache-Control", "no-cache", "no-store", "must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "no-sniff")
                    .build();
        } catch (DataAccessException e) {
            LOGGER.severe("Database connectivity check failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header("Cache-Control", "no-cache", "no-store", "must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "no-sniff")
                    .build();
        }
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<Void> methodNotAllowed() {
        LOGGER.warning("Unsupported HTTP method attempted on /healthz endpoint.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header("Cache-Control", "no-cache", "no-store", "must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "no-sniff")
                .build();
    }
}
