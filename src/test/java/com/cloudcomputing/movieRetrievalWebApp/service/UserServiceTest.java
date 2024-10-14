package com.cloudcomputing.movieRetrievalWebApp.service;

import com.cloudcomputing.movieRetrievalWebApp.dao.UserDAO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private UserDAO userDAO;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User();
    user.setEmailAddress("test@example.com");
    user.setPassword("password");
    user.setFirstName("John");
    user.setLastName("Doe");
  }

  @Test
  public void testGetAllUsers() {
    when(userDAO.getAllUsers()).thenReturn(Arrays.asList(user));

    List<User> users = userService.getAllUsers();

    assertEquals(1, users.size());
    verify(userDAO, times(1)).getAllUsers();
  }

  @Test
  public void testGetUserById() {
    when(userDAO.getUserById(1L)).thenReturn(Optional.of(user));

    Optional<User> foundUser = userService.getUserById(1L);

    assertTrue(foundUser.isPresent());
    assertEquals("test@example.com", foundUser.get().getEmailAddress());
  }

  @Test
  public void testGetUserByEmail() {
    when(userDAO.getAllUsers()).thenReturn(Arrays.asList(user));

    Optional<User> foundUser = userService.getUserByEmail("test@example.com");

    assertTrue(foundUser.isPresent());
    assertEquals("test@example.com", foundUser.get().getEmailAddress());
  }

  @Test
  public void testAddUser() {
    when(passwordEncoder.encode(any(String.class))).thenReturn("hashedPassword");
    when(userDAO.createUser(any(User.class))).thenReturn(user);

    User createdUser = userService.addUser(user);

    assertEquals(user, createdUser);
    assertEquals("hashedPassword", createdUser.getPassword());
    verify(passwordEncoder, times(1)).encode("password");
    verify(userDAO, times(1)).createUser(any(User.class));
  }

  @Test
  public void testUpdateUser() {
    when(passwordEncoder.encode(any(String.class))).thenReturn("hashedPassword");
    when(userDAO.updateUser(anyString(), any(User.class))).thenReturn(user);

    User updatedUser = userService.updateUser("test@example.com", user);

    assertEquals(user, updatedUser);
    verify(passwordEncoder, times(1)).encode("password");
    verify(userDAO, times(1)).updateUser(eq("test@example.com"), any(User.class));
  }

  @Test
  public void testDeleteUser() {
    doNothing().when(userDAO).deleteUser(anyString());

    userService.deleteUser("test@example.com");

    verify(userDAO, times(1)).deleteUser("test@example.com");
  }
}
