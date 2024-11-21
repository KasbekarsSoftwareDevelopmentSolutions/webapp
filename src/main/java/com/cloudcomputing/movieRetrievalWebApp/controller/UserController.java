package com.cloudcomputing.movieRetrievalWebApp.controller;

import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserCreateDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserResponseDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserUpdateDTO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.service.MessagePubService;
import com.cloudcomputing.movieRetrievalWebApp.service.UserService;
import com.cloudcomputing.movieRetrievalWebApp.service.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.timgroup.statsd.StatsDClient;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
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
  private VerificationService verificationService;

  @Autowired
  private MessagePubService messagePubService;

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
      // Publish message to SNS topic
      messagePubService.publishMessage(userResponseDTO.getFirst_name(), userResponseDTO.getId().toString());
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

    // Check if the user is verified
    ResponseEntity<Void> verificationCheck = checkUserVerified(principal);
    if (verificationCheck != null) {
      return new ResponseEntity<>(verificationCheck.getStatusCode());
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

    // Check if the user is verified
    ResponseEntity<Void> verificationCheck = checkUserVerified(principal);
    if (verificationCheck != null) {
      return new ResponseEntity<>(verificationCheck.getStatusCode());
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