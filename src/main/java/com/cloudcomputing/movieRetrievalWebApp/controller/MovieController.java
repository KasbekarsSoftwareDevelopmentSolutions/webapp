package com.cloudcomputing.movieRetrievalWebApp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MovieController {

  @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE,
      RequestMethod.PATCH })
  public ResponseEntity<Void> resourceNotAvailable() {
    // Return 404 Not Found for any HTTP method on "/"
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }
}
