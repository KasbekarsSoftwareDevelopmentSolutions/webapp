package com.cloudcomputing.movieRetrievalWebApp.controller;

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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
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

    when(userService.getUserByEmail(any())).thenReturn(Optional.empty());
    when(userService.addUser(any())).thenReturn(new User());

    mockMvc.perform(post("/v1/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userCreateDTO)))
        .andExpect(status().isCreated());

    verify(userService, times(1)).addUser(any());
  }

  @Test
  public void createUser_invalidEmail_returnsBadRequest() throws Exception {
    UserCreateDTO userCreateDTO = new UserCreateDTO();
    userCreateDTO.setEmailAddress("invalid-email");
    userCreateDTO.setPassword("password123");

    mockMvc.perform(post("/v1/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userCreateDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "usertest1@gmail.com", password = "userTest1")
  public void getUserInfo_authenticatedUser_returnsUserInfo() throws Exception {
    User user = new User();
    user.setEmailAddress("usertest1@gmail.com");
    user.setFirstName("User");
    user.setLastName("Test");
    user.setPassword("userTest1");

    when(userService.getUserByEmail("usertest1@gmail.com")).thenReturn(Optional.of(user));

    mockMvc.perform(get("/v1/user/self"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("usertest1@gmail.com"))
        .andExpect(jsonPath("$.first_name").value("User"))
        .andExpect(jsonPath("$.last_name").value("Test"));
  }

  @Test
  public void getUserInfo_unauthenticated_returnsUnauthorized() throws Exception {
    mockMvc.perform(get("/v1/user/self"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "usertest1@gmail.com", password = "userTest1")
  public void updateUser_authenticatedUser_returnsUpdatedInfo() throws Exception {
    UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
    userUpdateDTO.setEmailAddress("usertest1@gmail.com");
    userUpdateDTO.setFirstName("UserUpdated");
    userUpdateDTO.setLastName("TestUpdated");
    userUpdateDTO.setPassword("newPassword");

    User existingUser = new User();
    existingUser.setEmailAddress("usertest1@gmail.com");
    existingUser.setFirstName("UserUpdated");
    existingUser.setLastName("TestUpdated");

    when(userService.getUserByEmail("usertest1@gmail.com")).thenReturn(Optional.of(existingUser));
    when(userService.updateUser(eq("usertest1@gmail.com"), any())).thenReturn(existingUser);

    mockMvc.perform(put("/v1/user/self")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userUpdateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("usertest1@gmail.com"))
        .andExpect(jsonPath("$.first_name").value("UserUpdated"))
        .andExpect(jsonPath("$.last_name").value("TestUpdated"));
  }

  @Test
  @WithMockUser(username = "usertest1@gmail.com", password = "userTest1")
  public void updateUser_userNotFound_returnsNotFound() throws Exception {
    UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
    userUpdateDTO.setEmailAddress("nonexistentuser@gmail.com");

    when(userService.getUserByEmail("usertest1@gmail.com")).thenReturn(Optional.empty());

    mockMvc.perform(put("/v1/user/self")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userUpdateDTO)))
        .andExpect(status().isNotFound());
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
