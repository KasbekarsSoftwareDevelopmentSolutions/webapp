package com.cloudcomputing.movieRetrievalWebApp.controller;

import java.util.Optional;
import java.util.regex.Pattern;

import java.util.logging.Logger;

import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserCreateDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserResponseDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserUpdateDTO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.service.UserService;

/**
 * ControllerUtils is a utility class containing reusable methods for
 * validating, creating, updating, and retrieving users.
 */
public class ControllerUtils {

  // Logger instance for logging warnings and information.
  private static final Logger LOGGER = Logger.getLogger(ControllerUtils.class.getName());

  /**
   * Checks if a user exists in the system using the provided email.
   *
   * @param userService The UserService to interact with user data.
   * @param email       The email address to check.
   * @return Boolean indicating whether the user exists.
   */
  public static Boolean checkUserExists(UserService userService, String email) {
    // Retrieve the existing user from the service.
    Optional<User> existingUser = getExsistingUser(userService, email);

    // If the user does not exist, log and return false.
    if (existingUser.isEmpty()) {
      LOGGER.info("User not found for email: " + email);
      return false;
    } else {
      LOGGER.warning("User found for email: " + email);
      return true;
    }
  }

  /**
   * Retrieves an existing user from the UserService by email.
   *
   * @param userService The UserService used to retrieve user data.
   * @param email       The email address to look up.
   * @return Optional<User> containing the user if found, or an empty Optional if
   *         not.
   */
  public static Optional<User> getExsistingUser(UserService userService, String email) {
    // Call the service method to get the user by email.
    Optional<User> existingUser = userService.getUserByEmail(email);
    return existingUser;
  }

  /**
   * Constructs a UserResponseDTO object from a given User.
   *
   * @param userPresent The Optional<User> from which the response will be built.
   * @return UserResponseDTO containing the user details.
   */
  public static UserResponseDTO setResponseObject(Optional<User> userPresent) {
    // Extract the user object from the Optional.
    User user = userPresent.get();

    // Create a new UserResponseDTO and populate its fields.
    UserResponseDTO userResponseDTO = new UserResponseDTO();
    userResponseDTO.setId(user.getUserId());
    userResponseDTO.setFirst_name(user.getFirstName());
    userResponseDTO.setLast_name(user.getLastName());
    userResponseDTO.setEmail(user.getEmailAddress());
    userResponseDTO.setAccount_created(user.getAccountCreated().toString());
    userResponseDTO.setAccount_updated(user.getAccountUpdated().toString());

    return userResponseDTO;
  }

  /**
   * Validates the email format and password of a UserCreateDTO object.
   *
   * @param userCreateDTO The DTO containing user data to validate.
   * @return Boolean indicating whether the email and password are valid.
   */
  public static Boolean validateEmailPassword(UserCreateDTO userCreateDTO) {
    // Regular expression for validating email format.
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    Pattern pattern = Pattern.compile(emailRegex);

    // Check if the email matches the valid pattern.
    if (!pattern.matcher(userCreateDTO.getEmailAddress()).matches()) {
      LOGGER.warning("Invalid email format: " + userCreateDTO.getEmailAddress());
      return false;
    } else {
      LOGGER.info("Valid email provided: " + userCreateDTO.getEmailAddress());

      // Check if the password is provided and is not empty.
      if (userCreateDTO.getPassword() == null || userCreateDTO.getPassword().isEmpty()) {
        LOGGER.warning("Missing password for user: " + userCreateDTO.getEmailAddress());
        return false;
      }
      LOGGER.info("Valid password provided");
      return true;
    }
  }

  /**
   * Creates a new User object from a UserCreateDTO.
   *
   * @param userCreateDTO The DTO containing information about the user to create.
   * @return User object that represents the newly created user.
   */
  public static User createUser(UserCreateDTO userCreateDTO) {
    // Instantiate a new User and set its properties.
    User newUser = new User();
    newUser.setEmailAddress(userCreateDTO.getEmailAddress());
    newUser.setPassword(userCreateDTO.getPassword());
    newUser.setFirstName(userCreateDTO.getFirstName());
    newUser.setLastName(userCreateDTO.getLastName());

    LOGGER.info("New User Obj created: " + newUser);
    return newUser;
  }

  /**
   * Updates an existing User object with data from a UserUpdateDTO.
   *
   * @param userService   The UserService to retrieve the existing user.
   * @param userUpdateDTO DTO containing the updated user data.
   * @param email         The email of the user to update.
   * @return User object with updated information.
   */
  public static User updateUser(UserService userService, UserUpdateDTO userUpdateDTO, String email) {
    // Retrieve the existing user using the service.
    Optional<User> existingUserInfo = getExsistingUser(userService, email);
    User existingUser = existingUserInfo.get();
    User updatedUserInfo = new User();

    // Update first name if provided, otherwise retain the existing one.
    if (userUpdateDTO.getFirstName() != null && !userUpdateDTO.getFirstName().isEmpty()) {
      updatedUserInfo.setFirstName(userUpdateDTO.getFirstName());
    } else {
      updatedUserInfo.setFirstName(existingUser.getFirstName());
    }

    // Update last name if provided, otherwise retain the existing one.
    if (userUpdateDTO.getLastName() != null && !userUpdateDTO.getLastName().isEmpty()) {
      updatedUserInfo.setLastName(userUpdateDTO.getLastName());
    } else {
      updatedUserInfo.setLastName(existingUser.getLastName());
    }

    // Update password if provided, otherwise retain the existing one.
    if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
      updatedUserInfo.setPassword(userUpdateDTO.getPassword());
    } else {
      updatedUserInfo.setPassword(existingUser.getPassword());
    }

    return updatedUserInfo;
  }
}