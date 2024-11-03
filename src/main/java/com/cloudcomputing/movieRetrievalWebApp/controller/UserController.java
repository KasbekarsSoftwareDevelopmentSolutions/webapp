package com.cloudcomputing.movieRetrievalWebApp.controller;

import com.cloudcomputing.movieRetrievalWebApp.dto.imagedto.ImageResponseDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserCreateDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserResponseDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserUpdateDTO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.service.ImageService;
import com.cloudcomputing.movieRetrievalWebApp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;

/**
 * UserController handles API requests related to user operations such as
 * creating, retrieving, updating, and deleting user information.
 */
@RestController
@RequestMapping("/v1/user")
public class UserController {

  private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

  @Autowired
  private StatsDClient statsDClient;

  @Autowired
  private UserService userService;

  @Autowired
  private ImageService imageService;

  /**
   * Handles the POST request to create a new user.
   *
   * @param userCreateDTO DTO containing information required to create a new
   *                      user.
   * @return ResponseEntity containing the created UserResponseDTO and HTTP
   * status.
   */
  @PostMapping
  public ResponseEntity<UserResponseDTO> createUser(@RequestBody Map<String, Object> requestBodyMap,
                                                    HttpServletRequest request) {
    long startTime = System.currentTimeMillis();
    statsDClient.incrementCounter("api.v1.user.createUser.count");
    LOGGER.info("POST Request Received.");

    // Log query parameters if present
    request.getParameterMap()
            .forEach((key, value) -> LOGGER.warning("Query Parameter: " + key + " = " + String.join(",", value)));

    // Check if there are any query parameters, return BAD_REQUEST if found
    if (!request.getParameterMap().isEmpty()) {
      LOGGER.warning("Query parameters are not allowed in this request.");

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.createUser.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Define the expected fields
    Set<String> expectedFields = Set.of("emailAddress", "password", "firstName", "lastName");

    // Check for any extra fields in the request body
    Set<String> actualFields = requestBodyMap.keySet();
    if (!expectedFields.containsAll(actualFields) || actualFields.size() > expectedFields.size()) {
      LOGGER.warning("Request body contains extra or invalid fields.");

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.createUser.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Convert the request body to UserCreateDTO
    ObjectMapper mapper = new ObjectMapper();
    UserCreateDTO userCreateDTO = mapper.convertValue(requestBodyMap, UserCreateDTO.class);

    // Validate the email and password in the incoming request.
    if (!ControllerUtils.validateEmailPassword(userCreateDTO)) {
      LOGGER.warning("Email Address or Password input validation failed.");

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.createUser.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    String email = userCreateDTO.getEmailAddress();

    // Check if a user with the given email already exists.
    if (ControllerUtils.checkUserExists(userService, email)) {
      LOGGER.warning("User already exists: " + email);

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.createUser.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Create a new User object and add it to the service.
    User user = ControllerUtils.createUser(userCreateDTO);
    userService.addUser(user);

    // Prepare the response DTO with the created user details.
    Optional<User> justAddedUser = ControllerUtils.getExsistingUser(userService, email);
    if (justAddedUser.isPresent()) {
      UserResponseDTO userResponseDTO = ControllerUtils
              .setResponseObject(justAddedUser);
      // Log successful user creation and return the response.
      LOGGER.info("User created successfully: " + userResponseDTO);

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.createUser.response_time", elapsedTime);

      return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    } else {
      LOGGER.warning("User Creation Failed.");

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.createUser.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.CREATED);
    }
  }

  /**
   * Handles the GET request to retrieve information about the currently
   * authenticated user.
   *
   * @param request   The HTTP request object.
   * @param principal Security principal object containing user credentials.
   * @return ResponseEntity containing the UserResponseDTO and HTTP status.
   */
  @GetMapping("/self")
  public ResponseEntity<UserResponseDTO> getUserInfo(HttpServletRequest request, Principal principal) {

    long startTime = System.currentTimeMillis();
    statsDClient.incrementCounter("api.v1.user.getUserInfo.count");
    LOGGER.info("GET Request Received.");

    // Log query parameters if present
    request.getParameterMap()
            .forEach((key, value) -> LOGGER.warning("Query Parameter: " + key + " = " + String.join(",", value)));

    // Check if there are any query parameters, return BAD_REQUEST if found
    if (!request.getParameterMap().isEmpty()) {
      LOGGER.warning("Query parameters are not allowed in this request.");

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.getUserInfo.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    String email = principal.getName();

    // Check if the authenticated user exists in the system.
    if (ControllerUtils.checkUserExists(userService, email)) {

      Optional<User> existingUser = ControllerUtils.getExsistingUser(userService, email);
      UserResponseDTO userResponseDTO = ControllerUtils.setResponseObject(existingUser);

      // Log successful user retrieval and return the response.
      LOGGER.info("User info retrieved successfully: " + userResponseDTO);

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.getUserInfo.response_time", elapsedTime);

      return ResponseEntity.ok(userResponseDTO);
    }

    // Log if the user is not found and return a 404 response.
    LOGGER.warning("User not found for email: " + email);

    long elapsedTime = System.currentTimeMillis() - startTime;
    statsDClient.recordExecutionTime("api.v1.user.getUserInfo.response_time", elapsedTime);

    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * Handles the PUT request to update information of the currently authenticated
   * user.
   *
   * @param principal     Security principal object containing user credentials.
   * @param userUpdateDTO DTO containing updated user information.
   * @return ResponseEntity with HTTP status.
   */
  @PutMapping("/self")
  public ResponseEntity<UserResponseDTO> updateUser(Principal principal,
                                                    @RequestBody Map<String, Object> requestBodyMap,
                                                    HttpServletRequest request) {

    long startTime = System.currentTimeMillis();
    statsDClient.incrementCounter("api.v1.user.updateUser.count");
    LOGGER.info("PUT Request Received.");

    // Log query parameters if present
    request.getParameterMap()
            .forEach((key, value) -> LOGGER.warning("Query Parameter: " + key + " = " + String.join(",", value)));

    // Check if there are any query parameters, return BAD_REQUEST if found
    if (!request.getParameterMap().isEmpty()) {
      LOGGER.warning("Query parameters are not allowed in this request.");

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.updateUser.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    String email = principal.getName();

    // Check if the authenticated user exists in the system.
    if (!ControllerUtils.checkUserExists(userService, email)) {
      LOGGER.warning("User doesn't exist: " + email);

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.updateUser.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Define the allowed fields for the update
    Set<String> allowedFields = Set.of("password", "firstName", "lastName");

    // Check if the request body contains only the allowed fields
    Set<String> actualFields = requestBodyMap.keySet();
    if (!allowedFields.containsAll(actualFields) || actualFields.isEmpty()) {
      LOGGER.warning("Request body contains extra or invalid fields.");

      long elapsedTime = System.currentTimeMillis() - startTime;
      statsDClient.recordExecutionTime("api.v1.user.updateUser.response_time", elapsedTime);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Convert the request body to UserUpdateDTO
    ObjectMapper mapper = new ObjectMapper();
    UserUpdateDTO userUpdateDTO = mapper.convertValue(requestBodyMap, UserUpdateDTO.class);

    // Update the user information and save the changes in the service.
    User userToUpdate = ControllerUtils.updateUser(userService, userUpdateDTO, email);
    userService.updateUser(email, userToUpdate);

    // Log the successful update of user information.
    LOGGER.info("User Updated Successfully");

    long elapsedTime = System.currentTimeMillis() - startTime;
    statsDClient.recordExecutionTime("api.v1.user.updateUser.response_time", elapsedTime);

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PostMapping(value = "/self/pic", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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

  @GetMapping(value = "/self/pic", produces = MediaType.APPLICATION_JSON_VALUE)
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

  @DeleteMapping("/self/pic")
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
   * Handles unsupported HTTP methods (DELETE, PATCH, OPTIONS, HEAD) on the /self
   * endpoint.
   *
   * @return ResponseEntity with 405 Method Not Allowed and appropriate headers.
   */
  @RequestMapping(value = "/self", method = {
          RequestMethod.DELETE,
          RequestMethod.PATCH,
          RequestMethod.OPTIONS,
          RequestMethod.HEAD
  })
  public ResponseEntity<Void> methodNotAllowed() {
    // Log unsupported method attempts.
    LOGGER.warning("Unsupported HTTP method attempted on /self endpoint.");
    statsDClient.incrementCounter("api.v1.user.method_not_allowed.count");
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")
            .header("X-Content-Type-Options", "no-sniff")
            .build();
  }
}
