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
   *
   * @param principal Security principal containing user credentials.
   * @return ResponseEntity with 403 FORBIDDEN if the user is not verified, null otherwise.
   */
  private ResponseEntity<Void> checkUserVerified(Principal principal) {
    String email = principal.getName();
    Optional<User> user = userService.getUserByEmail(email);

    if (user.isEmpty()) {
      LOGGER.warning("User not found: " + email);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    boolean isVerified = verificationService.getVerificationTokenByUserId(user.get().getUserId().toString())
      .map(token -> token.getVerificationFlag() != null && token.getVerificationFlag())
      .orElse(false);

    if (!isVerified) {
      LOGGER.warning("User is not verified: " + email);
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    return null; // User is verified
  }

  /**
   * Uploads a profile image for the authenticated user.
   *
   * @param principal the security principal containing the user's email.
   * @param file      the image file to be uploaded.
   * @param request   the HTTP request object.
   * @return ResponseEntity containing the {@link ImageResponseDTO} on success or the appropriate HTTP status.
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

    // Check if the user is verified
    ResponseEntity<Void> verificationCheck = checkUserVerified(principal);
    if (verificationCheck != null) {
      return new ResponseEntity<>(verificationCheck.getStatusCode());
    }

    String email = principal.getName();

    // Check if the authenticated user exists in the system.
    if (ControllerUtils.checkUserExists(userService, email)) {
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
   *
   * @param principal      the security principal containing the user's email.
   * @param request        the HTTP request object.
   * @param requestBodyMap the request body, expected to be empty.
   * @return ResponseEntity containing the {@link ImageResponseDTO} on success or the appropriate HTTP status.
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

    // Check if the user is verified
    ResponseEntity<Void> verificationCheck = checkUserVerified(principal);
    if (verificationCheck != null) {
      return new ResponseEntity<>(verificationCheck.getStatusCode());
    }

    String email = principal.getName();

    // Check if the authenticated user exists in the system.
    if (ControllerUtils.checkUserExists(userService, email)) {
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
   *
   * @param principal      the security principal containing the user's email.
   * @param request        the HTTP request object.
   * @param requestBodyMap the request body, expected to be empty.
   * @return ResponseEntity with the appropriate HTTP status.
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

    // Check if the user is verified
    ResponseEntity<Void> verificationCheck = checkUserVerified(principal);
    if (verificationCheck != null) {
      return new ResponseEntity<>(verificationCheck.getStatusCode());
    }

    String email = principal.getName();

    // Check if the authenticated user exists in the system.
    if (ControllerUtils.checkUserExists(userService, email)) {
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
   * Handles unsupported HTTP methods (PUT, PATCH, OPTIONS, HEAD) on the /pic
   * endpoint.
   *
   * @return ResponseEntity with 405 Method Not Allowed and appropriate headers.
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