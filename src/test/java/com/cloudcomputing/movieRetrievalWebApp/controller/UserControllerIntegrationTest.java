package com.cloudcomputing.movieRetrievalWebApp.controller;

import com.cloudcomputing.movieRetrievalWebApp.config.StatsDConfig;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserCreateDTO;
import com.cloudcomputing.movieRetrievalWebApp.dto.userdto.UserUpdateDTO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(StatsDConfig.class)
public class UserControllerIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private UserService userService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  public void createUser_validRequest_returnsCreated() throws Exception {
    UserCreateDTO userCreateDTO = new UserCreateDTO();
    userCreateDTO.setEmailAddress("test@example.com");
    userCreateDTO.setPassword("password123");
    userCreateDTO.setFirstName("John");
    userCreateDTO.setLastName("Doe");
    String jsonRequest = new ObjectMapper().writeValueAsString(userCreateDTO);

    // Simulate the case where the user does not already exist
    when(userService.getUserByEmail(userCreateDTO.getEmailAddress())).thenReturn(Optional.empty());
    when(userService.addUser(any())).thenReturn(new User());

    // Perform the POST request and expect a 201 Created response
    mockMvc.perform(post("/v1/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonRequest))
        .andExpect(status().isCreated());

    verify(userService, times(1)).addUser(any());
  }

  @Test
  public void createUser_invalidEmail_returnsBadRequest() throws Exception {
    UserCreateDTO userCreateDTO = new UserCreateDTO();
    userCreateDTO.setEmailAddress("invalid-email");
    userCreateDTO.setPassword("password123");
    userCreateDTO.setFirstName("John");
    userCreateDTO.setLastName("Doe");

    mockMvc.perform(post("/v1/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userCreateDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createUser_existingUser_returnsBadRequest() throws Exception {
    UserCreateDTO userCreateDTO = new UserCreateDTO();
    userCreateDTO.setEmailAddress("existing@example.com");
    userCreateDTO.setPassword("password123");
    userCreateDTO.setFirstName("John");
    userCreateDTO.setLastName("Doe");

    when(userService.getUserByEmail(any())).thenReturn(Optional.of(new User()));

    mockMvc.perform(post("/v1/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userCreateDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createUser_withQueryParams_returnsBadRequest() throws Exception {
    UserCreateDTO userCreateDTO = new UserCreateDTO();
    userCreateDTO.setEmailAddress("test@example.com");
    userCreateDTO.setPassword("password123");
    userCreateDTO.setFirstName("John");
    userCreateDTO.setLastName("Doe");

    mockMvc.perform(post("/v1/user?param=value")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userCreateDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createUser_withExtraFields_returnsBadRequest() throws Exception {
    String json = "{"
        + "\"emailAddress\":\"test@example.com\","
        + "\"password\":\"password123\","
        + "\"firstName\":\"John\","
        + "\"lastName\":\"Doe\","
        + "\"extraField\":\"value\""
        + "}";

    mockMvc.perform(post("/v1/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isBadRequest());
  }

//  @Test
//  @WithMockUser(username = "usertest1@gmail.com", password = "userTest1")
//  public void getUserInfo_authenticatedUser_returnsUserInfo() throws Exception {
//    User user = new User();
//    user.setEmailAddress("usertest1@gmail.com");
//    user.setFirstName("User");
//    user.setLastName("Test");
//
//    when(userService.getUserByEmail("usertest1@gmail.com")).thenReturn(Optional.of(user));
//
//    mockMvc.perform(get("/v1/user/self"))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.email").value("usertest1@gmail.com"))
//        .andExpect(jsonPath("$.first_name").value("User"))
//        .andExpect(jsonPath("$.last_name").value("Test"));
//  }

  @Test
  public void getUserInfo_unauthenticated_returnsUnauthorized() throws Exception {
    mockMvc.perform(get("/v1/user/self"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "usertest1@gmail.com", password = "userTest1")
  public void getUserInfo_withQueryParams_returnsBadRequest() throws Exception {
    mockMvc.perform(get("/v1/user/self?param=value"))
        .andExpect(status().isBadRequest());
  }

//  @Test
//  @WithMockUser(username = "usertest1@gmail.com", password = "userTest1")
//  public void updateUser_authenticatedUser_returnsNoContent() throws Exception {
//    // Create a map to represent the request body
//    Map<String, Object> userUpdateMap = new HashMap<>();
//    userUpdateMap.put("firstName", "UserUpdated");
//    userUpdateMap.put("lastName", "TestUpdated");
//    userUpdateMap.put("password", "newPassword"); // This is allowed as per your controller
//
//    User existingUser = new User();
//    existingUser.setEmailAddress("usertest1@gmail.com");
//    existingUser.setFirstName("UserUpdated");
//    existingUser.setLastName("TestUpdated");
//
//    when(userService.getUserByEmail("usertest1@gmail.com")).thenReturn(Optional.of(existingUser));
//    when(userService.updateUser(eq("usertest1@gmail.com"), any())).thenReturn(existingUser);
//
//    mockMvc.perform(put("/v1/user/self")
//        .contentType(MediaType.APPLICATION_JSON)
//        .content(objectMapper.writeValueAsString(userUpdateMap))) // Send a Map instead of UserUpdateDTO
//        .andExpect(status().isNoContent());
//  }

  @Test
  @WithMockUser(username = "usertest1@gmail.com", password = "userTest1")
  public void updateUser_userNotFound_returnsNotFound() throws Exception {
    // Create a map to represent the request body
    Map<String, Object> userUpdateMap = new HashMap<>();
    userUpdateMap.put("firstName", "UserUpdated"); // Adding a valid field
    userUpdateMap.put("lastName", "TestUpdated"); // Adding a valid field

    // Mocking the user service to return an empty Optional
    when(userService.getUserByEmail("usertest1@gmail.com")).thenReturn(Optional.empty());

    mockMvc.perform(put("/v1/user/self")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userUpdateMap))) // Send a Map instead of UserUpdateDTO
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "usertest1@gmail.com", password = "userTest1")
  public void updateUser_withQueryParams_returnsBadRequest() throws Exception {
    UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
    userUpdateDTO.setEmailAddress("usertest1@gmail.com");

    mockMvc.perform(put("/v1/user/self?param=value")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userUpdateDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "usertest1@gmail.com", password = "userTest1")
  public void methodNotAllowed_returnsMethodNotAllowed() throws Exception {
    mockMvc.perform(patch("/v1/user/self"))
        .andExpect(status().isMethodNotAllowed());

    mockMvc.perform(delete("/v1/user/self"))
        .andExpect(status().isMethodNotAllowed());
  }
}
