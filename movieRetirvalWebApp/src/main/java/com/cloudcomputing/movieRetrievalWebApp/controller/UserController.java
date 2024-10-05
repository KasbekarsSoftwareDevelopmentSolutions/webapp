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

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserCreateDTO userCreateDTO) {
        LOGGER.info("Received request to create user with email: " + userCreateDTO.getEmailAddress());
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        if (!pattern.matcher(userCreateDTO.getEmailAddress()).matches()) {
            LOGGER.warning("Invalid email format for user creation: " + userCreateDTO.getEmailAddress() + " ##HttpStatus.BAD_REQUEST sent in response## ");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (userCreateDTO.getPassword() == null || userCreateDTO.getPassword().isEmpty()) {
            LOGGER.warning("Password is missing for user creation with email: " + userCreateDTO.getEmailAddress() + " ##HttpStatus.BAD_REQUEST sent in response## ");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setEmailAddress(userCreateDTO.getEmailAddress());
        user.setPassword(userCreateDTO.getPassword());
        user.setFirstName(userCreateDTO.getFirstName());
        user.setLastName(userCreateDTO.getLastName());

        Optional<User> existingUser = userService.getUserByEmail(user.getEmailAddress());
        if (existingUser.isPresent()) {
            LOGGER.warning("User with email " + user.getEmailAddress() + " already exists." + " ##HttpStatus.BAD_REQUEST sent in response## ");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        userService.addUser(user);
        LOGGER.info("User created successfully with email: " + user.getEmailAddress());

        Optional<User> findUser = userService.getUserByEmail(userCreateDTO.getEmailAddress());
        if (findUser.isPresent()) {
            User foundUser = findUser.get();
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            userResponseDTO.setId(foundUser.getUserId());
            userResponseDTO.setFirst_name(foundUser.getFirstName());
            userResponseDTO.setLast_name(foundUser.getLastName());
            userResponseDTO.setEmail(foundUser.getEmailAddress());
            userResponseDTO.setAccount_created(foundUser.getAccountCreated().toString());
            userResponseDTO.setAccount_updated(foundUser.getAccountUpdated().toString());

            LOGGER.info("User creation successful. Returning user details: " + userResponseDTO + " ##HttpStatus.CREATED sent in response## ");
            return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
        }

        LOGGER.severe("User creation failed, user could not be found after insertion.");
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/self")
    public ResponseEntity<UserResponseDTO> getUserInfo(HttpServletRequest request, Principal principal) {
        LOGGER.info("GET request received to retrieve user info.");
        if (principal == null) {
            LOGGER.warning("Unauthorized access attempt: Principal is null.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String email = principal.getName();

        List<User> users = userService.getAllUsers();

        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
            User foundUser = user.get();
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            userResponseDTO.setId(foundUser.getUserId());
            userResponseDTO.setFirst_name(foundUser.getFirstName());
            userResponseDTO.setLast_name(foundUser.getLastName());
            userResponseDTO.setEmail(foundUser.getEmailAddress());
            userResponseDTO.setAccount_created(foundUser.getAccountCreated().toString());
            userResponseDTO.setAccount_updated(foundUser.getAccountUpdated().toString());

            LOGGER.info("User info retrieved successfully." + userResponseDTO + " ##HttpStatus.OK sent in response## ");
            return ResponseEntity.ok(userResponseDTO);
        }

        LOGGER.warning("User not found for email: " + email);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/self")
    public ResponseEntity<UserResponseDTO> updateUser(Principal principal, @RequestBody UserUpdateDTO userUpdateDTO) {
        LOGGER.info("PUT request received to update user info for email: " + principal.getName());
        try {
            String email = principal.getName();

            Optional<User> existingUser = userService.getUserByEmail(email);
            if (existingUser.isEmpty()) {
                LOGGER.warning("User not found for update: " + email + " ##HttpStatus.NOT_FOUND sent in response## ");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            User userToUpdate = existingUser.get();
            userToUpdate.setEmailAddress(userUpdateDTO.getEmailAddress());
            userToUpdate.setPassword(userUpdateDTO.getPassword());
            userToUpdate.setFirstName(userUpdateDTO.getFirstName());
            userToUpdate.setLastName(userUpdateDTO.getLastName());

            userService.updateUser(email, userToUpdate);
            LOGGER.info("User info updated successfully for email: " + email);

            UserResponseDTO userResponseDTO = new UserResponseDTO();
            userResponseDTO.setId(userToUpdate.getUserId());
            userResponseDTO.setFirst_name(userToUpdate.getFirstName());
            userResponseDTO.setLast_name(userToUpdate.getLastName());
            userResponseDTO.setEmail(userToUpdate.getEmailAddress());
            userResponseDTO.setAccount_created(userToUpdate.getAccountCreated().toString());
            userResponseDTO.setAccount_updated(userToUpdate.getAccountUpdated().toString());

            LOGGER.info("Returning updated user details: " + userResponseDTO + " ##HttpStatus.OK sent in response## ");
            return ResponseEntity.ok(userResponseDTO);
        } catch (IllegalArgumentException e) {
            LOGGER.severe("Invalid input for user update: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/self", method = {
            RequestMethod.DELETE,
            RequestMethod.PATCH,
            RequestMethod.OPTIONS,
            RequestMethod.HEAD
    })
    public ResponseEntity<Void> methodNotAllowed() {
        LOGGER.warning("Unsupported HTTP method attempted on /self endpoint.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "no-sniff")
                .build();
    }
}
