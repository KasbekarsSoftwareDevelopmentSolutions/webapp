package com.cloudcomputing.movieRetrievalWebApp.controller;

import com.cloudcomputing.movieRetrievalWebApp.model.VerificationToken;
import com.cloudcomputing.movieRetrievalWebApp.service.VerificationService;
import com.timgroup.statsd.StatsDClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserCreateDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserResponseDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserUpdateDTO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.service.UserService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserControllerUnitTest {

  @Mock
  private UserService userService;

  @Mock
  private StatsDClient statsDClient;

  @Mock
  private VerificationService verificationService;

  @InjectMocks
  private UserController userController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createUser_Success() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("emailAddress", "test@example.com");
    requestBody.put("password", "password123");
    requestBody.put("firstName", "John");
    requestBody.put("lastName", "Doe");

    MockHttpServletRequest request = new MockHttpServletRequest();

    try (MockedStatic<ControllerUtils> mockedControllerUtils = mockStatic(ControllerUtils.class)) {
      mockedControllerUtils.when(() -> ControllerUtils.validateEmailPassword(any(UserCreateDTO.class)))
          .thenReturn(true);
      mockedControllerUtils.when(() -> ControllerUtils.checkUserExists(userService, "test@example.com"))
          .thenReturn(false);
      mockedControllerUtils.when(() -> ControllerUtils.createUser(any(UserCreateDTO.class))).thenReturn(new User());
      mockedControllerUtils.when(() -> ControllerUtils.getExsistingUser(userService, "test@example.com"))
          .thenReturn(Optional.of(new User()));
      mockedControllerUtils.when(() -> ControllerUtils.setResponseObject(any(Optional.class)))
          .thenReturn(new UserResponseDTO());

      ResponseEntity<UserResponseDTO> response = userController.createUser(requestBody, request);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
    }
  }

  @Test
  void createUser_UserAlreadyExists() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("emailAddress", "existing@example.com");
    requestBody.put("password", "password123");
    requestBody.put("firstName", "John");
    requestBody.put("lastName", "Doe");

    MockHttpServletRequest request = new MockHttpServletRequest();

    try (MockedStatic<ControllerUtils> mockedControllerUtils = mockStatic(ControllerUtils.class)) {
      mockedControllerUtils.when(() -> ControllerUtils.validateEmailPassword(any(UserCreateDTO.class)))
          .thenReturn(true);
      mockedControllerUtils.when(() -> ControllerUtils.checkUserExists(userService, "existing@example.com"))
          .thenReturn(true);

      ResponseEntity<UserResponseDTO> response = userController.createUser(requestBody, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Test
  void getUserInfo_Success() {
    Principal principal = () -> "test@example.com";
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();

    User mockUser = new User();
    mockUser.setEmailAddress("test@example.com");
    mockUser.setFirstName("John");
    mockUser.setLastName("Doe");

    VerificationToken token = new VerificationToken();
    token.setVerificationFlag(true);

    try (MockedStatic<ControllerUtils> mockedControllerUtils = mockStatic(ControllerUtils.class)) {
      mockedControllerUtils.when(() -> ControllerUtils.checkUserExists(userService, "test@example.com"))
        .thenReturn(true);
      mockedControllerUtils.when(() -> ControllerUtils.getExsistingUser(userService, "test@example.com"))
        .thenReturn(Optional.of(mockUser));
      mockedControllerUtils.when(() -> ControllerUtils.setResponseObject(any(Optional.class)))
        .thenReturn(new UserResponseDTO());

      // Mock userService to return the user
      when(userService.getUserByEmail("test@example.com"))
        .thenReturn(Optional.of(mockUser));

      // Mock verificationService to return a verified token
      when(verificationService.getVerificationTokenByUserId(mockUser.getUserId()))
        .thenReturn(Optional.of(token));

      ResponseEntity<UserResponseDTO> response = userController.getUserInfo(mockRequest, principal);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }
  }

  @Test
  void getUserInfo_UserNotFound() {
    Principal principal = () -> "nonexistent@example.com";
    MockHttpServletRequest request = new MockHttpServletRequest();

    try (MockedStatic<ControllerUtils> mockedControllerUtils = mockStatic(ControllerUtils.class)) {
      mockedControllerUtils.when(() -> ControllerUtils.checkUserExists(userService, "nonexistent@example.com"))
          .thenReturn(false);

      ResponseEntity<UserResponseDTO> response = userController.getUserInfo(request, principal);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Test
  void updateUser_Success() {
    Principal principal = () -> "test@example.com";
    Map<String, Object> mockRequestBody = new HashMap<>();
    mockRequestBody.put("firstName", "UpdatedJohn");
    mockRequestBody.put("lastName", "UpdatedDoe");

    MockHttpServletRequest mockRequest = new MockHttpServletRequest();

    User mockExistingUser = new User();
    mockExistingUser.setEmailAddress("test@example.com");

    VerificationToken mockVerificationToken = new VerificationToken();
    mockVerificationToken.setVerificationFlag(true);

    try (MockedStatic<ControllerUtils> mockedControllerUtils = mockStatic(ControllerUtils.class)) {
      mockedControllerUtils.when(() -> ControllerUtils.checkUserExists(userService, "test@example.com"))
        .thenReturn(true);
      mockedControllerUtils.when(() -> ControllerUtils.updateUser(eq(userService),
          any(UserUpdateDTO.class), eq("test@example.com")))
        .thenReturn(mockExistingUser);

      // Mock `userService.getUserByEmail` to return the user
      when(userService.getUserByEmail("test@example.com"))
        .thenReturn(Optional.of(mockExistingUser));

      // Mock `verificationService.getVerificationTokenByUserId` to return a verified token
      when(verificationService.getVerificationTokenByUserId(mockExistingUser.getUserId()))
        .thenReturn(Optional.of(mockVerificationToken));

      ResponseEntity<UserResponseDTO> response = userController.updateUser(principal, mockRequestBody, mockRequest);

      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
      verify(userService).updateUser(eq("test@example.com"), any(User.class));
    }
  }

  @Test
  void updateUser_UserNotFound() {
    Principal principal = () -> "nonexistent@example.com";
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("firstName", "UpdatedJohn");

    MockHttpServletRequest request = new MockHttpServletRequest();

    try (MockedStatic<ControllerUtils> mockedControllerUtils = mockStatic(ControllerUtils.class)) {
      mockedControllerUtils.when(() -> ControllerUtils.checkUserExists(userService, "nonexistent@example.com"))
          .thenReturn(false);

      ResponseEntity<UserResponseDTO> response = userController.updateUser(principal, requestBody, request);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Test
  void methodNotAllowed() {
    ResponseEntity<Void> response = userController.methodNotAllowed();

    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    assertTrue(response.getHeaders().containsKey("Cache-Control"));
    assertTrue(response.getHeaders().containsKey("Pragma"));
    assertTrue(response.getHeaders().containsKey("X-Content-Type-Options"));
  }

}