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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserControllerUnitTest {

  @Mock
  private UserService userService;

  @Mock
  private Principal principal;

  @InjectMocks
  private UserController userController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createUser_validRequest_returnsCreated() {
    UserCreateDTO userCreateDTO = new UserCreateDTO();
    userCreateDTO.setEmailAddress("test@example.com");
    userCreateDTO.setPassword("password123");
    userCreateDTO.setFirstName("John");
    userCreateDTO.setLastName("Doe");

    when(userService.getUserByEmail(anyString())).thenReturn(Optional.empty());
    when(userService.addUser(any(User.class))).thenReturn(new User());

    ResponseEntity<UserResponseDTO> response = userController.createUser(userCreateDTO);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("test@example.com", response.getBody().getEmail());
  }

  @Test
  void createUser_invalidEmail_returnsBadRequest() {
    UserCreateDTO userCreateDTO = new UserCreateDTO();
    userCreateDTO.setEmailAddress("invalid-email");
    userCreateDTO.setPassword("password123");

    ResponseEntity<UserResponseDTO> response = userController.createUser(userCreateDTO);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void createUser_existingUser_returnsBadRequest() {
    UserCreateDTO userCreateDTO = new UserCreateDTO();
    userCreateDTO.setEmailAddress("existing@example.com");
    userCreateDTO.setPassword("password123");

    when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(new User()));

    ResponseEntity<UserResponseDTO> response = userController.createUser(userCreateDTO);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void getUserInfo_existingUser_returnsOk() {
    User user = new User();
    user.setUserId(1L);
    user.setEmailAddress("test@example.com");
    user.setFirstName("John");
    user.setLastName("Doe");

    when(principal.getName()).thenReturn("test@example.com");
    when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));

    ResponseEntity<UserResponseDTO> response = userController.getUserInfo(null, principal);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("test@example.com", response.getBody().getEmail());
  }

  @Test
  void getUserInfo_nonExistingUser_returnsNotFound() {
    when(principal.getName()).thenReturn("nonexistent@example.com");
    when(userService.getUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    ResponseEntity<UserResponseDTO> response = userController.getUserInfo(null, principal);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void updateUser_existingUser_returnsOk() {
    UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
    userUpdateDTO.setEmailAddress("updated@example.com");
    userUpdateDTO.setFirstName("UpdatedJohn");
    userUpdateDTO.setLastName("UpdatedDoe");

    User existingUser = new User();
    existingUser.setUserId(1L);
    existingUser.setEmailAddress("test@example.com");

    when(principal.getName()).thenReturn("test@example.com");
    when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
    when(userService.updateUser(eq("test@example.com"), any(User.class))).thenReturn(existingUser);

    ResponseEntity<UserResponseDTO> response = userController.updateUser(principal, userUpdateDTO);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("updated@example.com", response.getBody().getEmail());
  }

  @Test
  void updateUser_nonExistingUser_returnsNotFound() {
    UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
    when(principal.getName()).thenReturn("nonexistent@example.com");
    when(userService.getUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    ResponseEntity<UserResponseDTO> response = userController.updateUser(principal, userUpdateDTO);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void methodNotAllowed_returnsMethodNotAllowed() {
    ResponseEntity<Void> response = userController.methodNotAllowed();

    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    assertNotNull(response.getHeaders());
    assertTrue(response.getHeaders().containsKey("Cache-Control"));
    assertTrue(response.getHeaders().containsKey("Pragma"));
    assertTrue(response.getHeaders().containsKey("X-Content-Type-Options"));
  }
}
