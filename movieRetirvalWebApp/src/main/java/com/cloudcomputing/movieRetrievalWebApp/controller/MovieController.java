package com.cloudcomputing.movieRetrievalWebApp.controller;

import com.cloudcomputing.movieRetrievalWebApp.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @RequestMapping(method = {RequestMethod.GET})
    public ResponseEntity<Void> methodAllowed(HttpServletRequest request) {
        if (request.getContentLength() > 0) {
            return ResponseEntity.badRequest()
                    .header("Cache-Control", "no-cache", "no-store", "must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "no-sniff")
                    .build();

        }
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache", "no-store", "must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "no-sniff")
                .build();
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
