package com.cloudcomputing.movieRetrievalWebApp.controller;

import com.cloudcomputing.movieRetrievalWebApp.dto.imagedto.ImageResponseDTO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.service.ImageService;
import com.cloudcomputing.movieRetrievalWebApp.service.UserService;

import com.cloudcomputing.movieRetrievalWebApp.service.VerificationService;
import com.timgroup.statsd.StatsDClient;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Controller class to handle image-related operations for authenticated users.
 * Provides endpoints for uploading, retrieving, and deleting user profile images.
 * Incorporates user verification and request validation, ensuring robust and secure interactions.
 * Includes response time tracking and query parameter validation for improved monitoring and security.
 */
@RestController
@RequestMapping("/v1/user/self")
public class ImageController {

  private static final Logger LOGGER = Logger.getLogger(ImageController.class.getName());

  @Autowired
  private StatsDClient statsDClient;

  @Autowired
  private UserService userService;

  @Autowired
  private ImageService imageService;

  @Autowired
  private VerificationService verificationService;

  /**
   * Middleware to check if the authenticated user is verified.
   * Verifies the user by retrieving the verification token associated with their account.
   * Logs warnings if the user is not found or verification fails.
   *
   * @param email The email of the authenticated user.
   * @return true if the user is verified, false otherwise.
   */
  private Boolean checkUserVerified(String email) {
    Optional<User> user = userService.getUserByEmail(email);

    if (user.isEmpty()) {
      LOGGER.warning("User not found: " + email);
    }

    return verificationService.getVerificationTokenByUserId(user.get().getUserId())
      .map(token -> token.getVerificationFlag() != null && token.getVerificationFlag())
      .orElse(false); // User is verified
  }

  /**
   * Uploads a profile image for the authenticated user.
   * Validates query parameters, checks user verification status, and processes the image upload.
   * Logs the request and records metrics for monitoring.
   *
   * @param principal The security principal containing the user's email.
   * @param file      The image file to be uploaded.
   * @param request   The HTTP request object, used to validate parameters.
   * @return ResponseEntity containing the {@link ImageResponseDTO} on success or the appropriate HTTP status code.
   */
  @PostMapping(value = "/pic", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ImageResponseDTO> uploadUserImage(Principal principal, @RequestParam("file") MultipartFile file,
                                                          HttpServletRequest request) {

    long startTime = System.currentTimeMillis();
    statsDClient.incrementCounter("api.v1.user.uploadUserImage.count");

    LOGGER.info("Image POST Request Received.");

    // Log query parameters if present
    request.getParameterMap()
      .forEach((key, value) -> LOGGER.warning("Query Parameter: " + key + " = " + String.join(",", value)));

    // Check if there are any query parameters, return BAD_REQUEST if found
    if (!request.getParameterMap().isEmpty()) {
      LOGGER.warning("Query parameters are not allowed in this request.");

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.uploadUserImage.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    String email = principal.getName();

    // Check if the authenticated user exists in the system.
    if (ControllerUtils.checkUserExists(userService, email)) {

      // Check if the user is verified
      if (!checkUserVerified(email)) {
        LOGGER.warning("User is not verified: " + email);
        long elapsedTime = System.currentTimeMillis() - startTime;
        statsDClient.recordExecutionTime("api.v1.user.getUserInfo.response_time", elapsedTime);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      LOGGER.info("User is verified: " + email);

      Optional<User> existingUser = ControllerUtils.getExsistingUser(userService, email);
      if (existingUser.isPresent()) {
        User user = existingUser.get();
        try {
          UUID userId = user.getUserId();
          ImageResponseDTO response = imageService.uploadImage(file, userId);

          long elapsedTime = System.currentTimeMillis() - startTime;
          statsDClient.recordExecutionTime("api.v1.user.uploadUserImage.response_time", elapsedTime);

          return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IOException e) {

          long elapsedTime = System.currentTimeMillis() - startTime;
          statsDClient.recordExecutionTime("api.v1.user.uploadUserImage.response_time", elapsedTime);

          return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
      }
    }
    // Log if the user is not found and return a 404 response.
    LOGGER.warning("User not found for email: " + email);

    long elapsedTime = System.currentTimeMillis() - startTime;
    statsDClient.recordExecutionTime("api.v1.user.uploadUserImage.response_time", elapsedTime);

    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * Retrieves the profile image of the authenticated user.
   * Validates query parameters and request body to ensure compliance with the API contract.
   * Logs request details and records response time metrics.
   *
   * @param principal      The security principal containing the user's email.
   * @param request        The HTTP request object, used to validate parameters.
   * @param requestBodyMap The request body, expected to be empty; validated for compliance.
   * @return ResponseEntity containing the {@link ImageResponseDTO} on success or the appropriate HTTP status code.
   */
  @GetMapping(value = "/pic", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ImageResponseDTO> getUserImage(Principal principal,  HttpServletRequest request,
                                                       @RequestBody Map<String, Object> requestBodyMap) {

    long startTime = System.currentTimeMillis();
    statsDClient.incrementCounter("api.v1.user.getUserImage.count");

    LOGGER.info("Image GET Request Received.");

    // Log query parameters if present
    request.getParameterMap()
      .forEach((key, value) -> LOGGER.warning("Query Parameter: " + key + " = " + String.join(",", value)));

    // Check if there are any query parameters, return BAD_REQUEST if found
    if (!request.getParameterMap().isEmpty()) {
      LOGGER.warning("Query parameters are not allowed in this request.");

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.getUserImage.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Check for any fields in the request body; return BAD_REQUEST if found
    if (!requestBodyMap.isEmpty()) {
      LOGGER.warning("Request body should not contain any fields.");

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.getUserImage.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    String email = principal.getName();

    // Check if the authenticated user exists in the system.
    if (ControllerUtils.checkUserExists(userService, email)) {

      // Check if the user is verified
      if (!checkUserVerified(email)) {
        LOGGER.warning("User is not verified: " + email);
        long elapsedTime = System.currentTimeMillis() - startTime;
        statsDClient.recordExecutionTime("api.v1.user.getUserInfo.response_time", elapsedTime);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      LOGGER.info("User is verified: " + email);

      Optional<User> existingUser = ControllerUtils.getExsistingUser(userService, email);
      if (existingUser.isPresent()) {
        User user = existingUser.get();
        try {
          UUID userId = user.getUserId();
          ImageResponseDTO imageResponseData = imageService.downloadImage(userId);
          LOGGER.info("Request Successful. Returning ImageResponseDTO.");

          long elapsedTime = System.currentTimeMillis() - startTime;
          statsDClient.recordExecutionTime("api.v1.user.getUserImage.response_time", elapsedTime);

          return new ResponseEntity<>(imageResponseData, HttpStatus.OK);
        } catch (IOException e) {
          LOGGER.warning("Error fetching image: " + e.getMessage());

          long elapsedTime = System.currentTimeMillis() - startTime;
          statsDClient.recordExecutionTime("api.v1.user.getUserImage.response_time", elapsedTime);

          return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
      }
    } else {

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.getUserImage.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    long elapsedTime = System.currentTimeMillis() - startTime;
    statsDClient.recordExecutionTime("api.v1.user.getUserImage.response_time", elapsedTime);

    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Deletes the profile image of the authenticated user.
   * Validates query parameters and request body, checks user verification status, and processes the image deletion.
   * Logs request details and tracks response time metrics for monitoring.
   *
   * @param principal      The security principal containing the user's email.
   * @param request        The HTTP request object, used to validate parameters.
   * @param requestBodyMap The request body, expected to be empty; validated for compliance.
   * @return ResponseEntity with the appropriate HTTP status code.
   */
  @DeleteMapping("/pic")
  public ResponseEntity<Void> deleteUserImage(Principal principal,  HttpServletRequest request,
                                              @RequestBody Map<String, Object> requestBodyMap) {
    long startTime = System.currentTimeMillis();
    statsDClient.incrementCounter("api.v1.user.deleteUserImage.count");
    LOGGER.info("Image DELETE Request Received.");

    // Log query parameters if present
    request.getParameterMap()
      .forEach((key, value) -> LOGGER.warning("Query Parameter: " + key + " = " + String.join(",", value)));

    // Check if there are any query parameters, return BAD_REQUEST if found
    if (!request.getParameterMap().isEmpty()) {
      LOGGER.warning("Query parameters are not allowed in this request.");

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.deleteUserImage.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Check for any fields in the request body; return BAD_REQUEST if found
    if (!requestBodyMap.isEmpty()) {
      LOGGER.warning("Request body should not contain any fields.");

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.deleteUserImage.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    String email = principal.getName();

    // Check if the authenticated user exists in the system.
    if (ControllerUtils.checkUserExists(userService, email)) {

      // Check if the user is verified
      if (!checkUserVerified(email)) {
        LOGGER.warning("User is not verified: " + email);
        long elapsedTime = System.currentTimeMillis() - startTime;
        statsDClient.recordExecutionTime("api.v1.user.getUserInfo.response_time", elapsedTime);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      LOGGER.info("User is verified: " + email);

      Optional<User> existingUser = ControllerUtils.getExsistingUser(userService, email);
      if (existingUser.isPresent()) {
        User user = existingUser.get();
        try {
          UUID userId = user.getUserId();
          imageService.deleteImage(userId);
          LOGGER.info("Request Successful. Image Deleted Successfully.");

          long elapsedTime = System.currentTimeMillis() - startTime;
          statsDClient.recordExecutionTime("api.v1.user.deleteUserImage.response_time", elapsedTime);

          return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IOException e) {
          LOGGER.warning("Error Deleting image: " + e.getMessage());

          long elapsedTime = System.currentTimeMillis() - startTime;
          statsDClient.recordExecutionTime("api.v1.user.deleteUserImage.response_time", elapsedTime);

          return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
      }
    } else {

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.deleteUserImage.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    long elapsedTime = System.currentTimeMillis() - startTime;
    statsDClient.recordExecutionTime("api.v1.user.deleteUserImage.response_time", elapsedTime);

    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles unsupported HTTP methods (PUT, PATCH, OPTIONS, HEAD) on the /pic endpoint.
   * Responds with 405 Method Not Allowed and includes appropriate headers to ensure no caching.
   * Logs any attempts to use unsupported methods and tracks metrics for monitoring.
   *
   * @return ResponseEntity with 405 Method Not Allowed status and appropriate headers.
   */
  @RequestMapping(value = "/pic", method = {
    RequestMethod.PUT,
    RequestMethod.PATCH,
    RequestMethod.OPTIONS,
    RequestMethod.HEAD
  })
  public ResponseEntity<Void> methodNotAllowed() {
    // Log unsupported method attempts.
    LOGGER.warning("Unsupported HTTP method attempted on /pic endpoint.");
    statsDClient.incrementCounter("api.v1.image.method_not_allowed.count");
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
      .header("Cache-Control", "no-cache, no-store, must-revalidate")
      .header("Pragma", "no-cache")
      .header("X-Content-Type-Options", "no-sniff")
      .build();
  }
}