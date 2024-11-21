package com.cloudcomputing.movieRetrievalWebApp.controller;

import com.cloudcomputing.movieRetrievalWebApp.service.VerificationService;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("/v1/user")
public class VerificationController {

  private static final Logger LOGGER = Logger.getLogger(VerificationController.class.getName());

  private final VerificationService verificationService;

  @Autowired
  private StatsDClient statsDClient;

  @Autowired
  public VerificationController(VerificationService verificationService) {
    this.verificationService = verificationService;
  }

  /**
   * GET endpoint to verify a token.
   *
   * @param token The token to verify, passed as a query parameter.
   * @return ResponseEntity with status OK if verified, or BAD_REQUEST otherwise.
   */
  @GetMapping("/verify")
  public ResponseEntity<String> verifyToken(@RequestParam("token") String token) {
    long startTime = System.currentTimeMillis();
    statsDClient.incrementCounter("api.v1.user.verifyToken.count");
    LOGGER.info("GET Request to /v1/user/verify received with token: " + token);

    if (token == null || token.isEmpty()) {
      LOGGER.warning("Token is missing or empty.");
      statsDClient.recordExecutionTime("api.v1.user.verifyToken.response_time", System.currentTimeMillis() - startTime);
      return new ResponseEntity<>("Token is missing or invalid.", HttpStatus.BAD_REQUEST);
    }

    boolean isVerified = verificationService.verifyToken(token);

    if (isVerified) {
      LOGGER.info("Token verified successfully.");
      statsDClient.recordExecutionTime("api.v1.user.verifyToken.response_time", System.currentTimeMillis() - startTime);
      return new ResponseEntity<>("Token verified successfully.", HttpStatus.OK);
    } else {
      LOGGER.warning("Token verification failed.");
      statsDClient.recordExecutionTime("api.v1.user.verifyToken.response_time", System.currentTimeMillis() - startTime);
      return new ResponseEntity<>("Token verification failed or token expired.", HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Handle all unsupported HTTP methods (POST, PUT, DELETE, etc.) on /verify.
   *
   * @return ResponseEntity with status BAD_REQUEST.
   */
  @RequestMapping(value = "/verify", method = {
    RequestMethod.POST,
    RequestMethod.PUT,
    RequestMethod.DELETE,
    RequestMethod.PATCH,
    RequestMethod.OPTIONS,
    RequestMethod.HEAD
  })
  public ResponseEntity<String> handleUnsupportedMethods() {
    LOGGER.warning("Unsupported HTTP method attempted on /v1/user/verify endpoint.");
    statsDClient.incrementCounter("api.v1.user.verifyToken.unsupported_method.count");
    return new ResponseEntity<>("Bad request: Unsupported HTTP method.", HttpStatus.BAD_REQUEST);
  }
}
