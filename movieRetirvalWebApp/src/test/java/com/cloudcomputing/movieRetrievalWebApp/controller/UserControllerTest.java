package com.cloudcomputing.movieRetrievalWebApp.controller;

import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserCreateDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserResponseDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserUpdateDTO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private Principal principal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test 1: Create User - Success
    @Test
    void testCreateUser_Success() {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmailAddress("test@example.com");
        userCreateDTO.setPassword("password");
        userCreateDTO.setFirstName("John");
        userCreateDTO.setLastName("Doe");

        User user = new User();
        user.setEmailAddress("test@example.com");
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");

        when(userService.getUserByEmail(userCreateDTO.getEmailAddress())).thenReturn(Optional.empty());
        when(userService.getUserByEmail(userCreateDTO.getEmailAddress())).thenReturn(Optional.of(user));

        ResponseEntity<UserResponseDTO> response = userController.createUser(userCreateDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test@example.com", response.getBody().getEmail());
    }

    // Test 2: Create User - Bad Request due to Invalid Email
    @Test
    void testCreateUser_InvalidEmail() {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmailAddress("invalid-email");
        userCreateDTO.setPassword("password");

        ResponseEntity<UserResponseDTO> response = userController.createUser(userCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // Test 3: Create User - Bad Request due to Empty Password
    @Test
    void testCreateUser_EmptyPassword() {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmailAddress("test@example.com");
        userCreateDTO.setPassword(""); // Empty password

        ResponseEntity<UserResponseDTO> response = userController.createUser(userCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // Test 4: Create User - User Already Exists
    @Test
    void testCreateUser_UserAlreadyExists() {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmailAddress("test@example.com");
        userCreateDTO.setPassword("password");

        User existingUser = new User();
        when(userService.getUserByEmail(userCreateDTO.getEmailAddress())).thenReturn(Optional.of(existingUser));

        ResponseEntity<UserResponseDTO> response = userController.createUser(userCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // Test 5: Get User Info - Success
    @Test
    void testGetUserInfo_Success() {
        when(principal.getName()).thenReturn("test@example.com");

        User user = new User();
        user.setEmailAddress("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<UserResponseDTO> response = userController.getUserInfo(null, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test@example.com", response.getBody().getEmail());
    }

    // Test 6: Get User Info - Unauthorized
    @Test
    void testGetUserInfo_Unauthorized() {
        ResponseEntity<UserResponseDTO> response = userController.getUserInfo(null, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // Test 7: Get User Info - Not Found
    @Test
    void testGetUserInfo_NotFound() {
        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.empty());

        ResponseEntity<UserResponseDTO> response = userController.getUserInfo(null, principal);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Test 8: Update User - Success
    @Test
    void testUpdateUser_Success() {
        when(principal.getName()).thenReturn("test@example.com");

        User user = new User();
        user.setEmailAddress("test@example.com");
        user.setPassword("old_password");

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setEmailAddress("new_email@example.com");
        userUpdateDTO.setPassword("new_password");

        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<UserResponseDTO> response = userController.updateUser(principal, userUpdateDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // Test 9: Update User - User Not Found
    @Test
    void testUpdateUser_UserNotFound() {
        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.empty());

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        ResponseEntity<UserResponseDTO> response = userController.updateUser(principal, userUpdateDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Test 10: Method Not Allowed
    @Test
    void testMethodNotAllowed() {
        ResponseEntity<Void> response = userController.methodNotAllowed();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }
}

