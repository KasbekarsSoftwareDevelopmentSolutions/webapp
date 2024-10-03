package com.cloudcomputing.movieRetrievalWebApp.controller;

import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserCreateDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserResponseDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserUpdateDTO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/v1/user")
public class UserController {
    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

    @Autowired
    private UserService userService;

    // Create a new user
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserCreateDTO userCreateDTO) {
        // Validate email format using regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        if (!pattern.matcher(userCreateDTO.getEmailAddress()).matches()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Validate password is not empty or null
        if (userCreateDTO.getPassword() == null || userCreateDTO.getPassword().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Convert UserCreateDTO to User model
        User user = new User();
        user.setEmailAddress(userCreateDTO.getEmailAddress());
        user.setPassword(userCreateDTO.getPassword());
        user.setFirstName(userCreateDTO.getFirstName());
        user.setLastName(userCreateDTO.getLastName());

        Optional<User> existingUser = userService.getUserByEmail(user.getEmailAddress());
        if (existingUser.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Add the new user to the database
        userService.addUser(user);

        // Retrieve the newly created user
        Optional<User> findUser = userService.getUserByEmail(userCreateDTO.getEmailAddress());
        if (findUser.isPresent()) {
            User foundUser = findUser.get();
            // Map User to UserResponseDTO
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            userResponseDTO.setId(foundUser.getUserId());
            userResponseDTO.setFirst_name(foundUser.getFirstName());
            userResponseDTO.setLast_name(foundUser.getLastName());
            userResponseDTO.setEmail(foundUser.getEmailAddress());
            userResponseDTO.setAccount_created(foundUser.getAccountCreated().toString());
            userResponseDTO.setAccount_updated(foundUser.getAccountUpdated().toString());

            // Return 201 CREATED status and user details
            return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
        }

        // If something went wrong and the user is not found
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Get user information
    @GetMapping("/self")
    public ResponseEntity<UserResponseDTO> getUserInfo(HttpServletRequest request, Principal principal) {
        LOGGER.info("Received request to get user info:" + request);
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String email = principal.getName();

        List<User> users = userService.getAllUsers();
        LOGGER.info("Received request to get user info:" + request);

        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
            User foundUser = user.get();
            // Map User to UserResponseDTO
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            userResponseDTO.setId(foundUser.getUserId());
            userResponseDTO.setFirst_name(foundUser.getFirstName());
            userResponseDTO.setLast_name(foundUser.getLastName());
            userResponseDTO.setEmail(foundUser.getEmailAddress());
            userResponseDTO.setAccount_created(foundUser.getAccountCreated().toString());
            userResponseDTO.setAccount_updated(foundUser.getAccountUpdated().toString());
            return ResponseEntity.ok(userResponseDTO);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Update user information
    @PutMapping("/self")
    public ResponseEntity<UserResponseDTO> updateUser(Principal principal, @RequestBody UserUpdateDTO userUpdateDTO) {
        try {
            // Fetch the current user's email from Principal
            String email = principal.getName();

            // Retrieve the user before updating to ensure they exist
            Optional<User> existingUser = userService.getUserByEmail(email);
            if (existingUser.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Update the user's information
            User userToUpdate = existingUser.get();
            userToUpdate.setEmailAddress(userUpdateDTO.getEmailAddress());
            userToUpdate.setPassword(userUpdateDTO.getPassword());
            userToUpdate.setFirstName(userUpdateDTO.getFirstName());
            userToUpdate.setLastName(userUpdateDTO.getLastName());

            // Save the updated user
            userService.updateUser(email, userToUpdate);

            // Map the updated user to UserResponseDTO
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            userResponseDTO.setId(userToUpdate.getUserId());
            userResponseDTO.setFirst_name(userToUpdate.getFirstName());
            userResponseDTO.setLast_name(userToUpdate.getLastName());
            userResponseDTO.setEmail(userToUpdate.getEmailAddress());
            userResponseDTO.setAccount_created(userToUpdate.getAccountCreated().toString());
            userResponseDTO.setAccount_updated(userToUpdate.getAccountUpdated().toString());

            return ResponseEntity.ok(userResponseDTO);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Allow only unsupported HTTP methods
    @RequestMapping(value = "/self", method = {
            RequestMethod.DELETE,
            RequestMethod.PATCH,
            RequestMethod.OPTIONS,
            RequestMethod.HEAD
    })
    public ResponseEntity<Void> methodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "no-sniff")
                .build();
    }
}
