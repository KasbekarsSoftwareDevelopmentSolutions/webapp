package com.cloudcomputing.movieRetrievalWebApp.controller;

import com.cloudcomputing.movieRetrievalWebApp.service.VerificationService;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * VerificationController handles API requests related to user verification.
 *
 * It provides endpoints to verify tokens and handle unsupported HTTP methods
 * gracefully for the verification process. The controller integrates with
 * the VerificationService to validate tokens and uses StatsDClient for
 * performance metrics and logging.
 */
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
   * GET endpoint to verify a provided token.
   *
   * This endpoint accepts a token as a query parameter, validates it through
   * the VerificationService, and returns an appropriate response. It also
   * logs the request details and captures metrics for monitoring purposes.
   *
   * @param token The token to be verified, provided as a query parameter.
   * @return ResponseEntity with:
   *         - HTTP 200 (OK) if the token is successfully verified.
   *         - HTTP 400 (BAD_REQUEST) if the token is invalid, expired, or missing.
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

    if(verificationService.isTokenAlreadyVerified(UUID.fromString(token))) {
      LOGGER.warning("Token already verified.");
      return new ResponseEntity<>("Token already verified.", HttpStatus.BAD_REQUEST);
    }

    boolean isVerified = verificationService.verifyToken(UUID.fromString(token));

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
   * Handles unsupported HTTP methods on the /verify endpoint.
   *
   * This method catches all unsupported HTTP methods such as POST, PUT, DELETE,
   * and others, providing a consistent response with HTTP 400 (BAD_REQUEST).
   * It logs the attempt and increments a metric counter.
   *
   * @return ResponseEntity with HTTP 400 (BAD_REQUEST) and a descriptive message.
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