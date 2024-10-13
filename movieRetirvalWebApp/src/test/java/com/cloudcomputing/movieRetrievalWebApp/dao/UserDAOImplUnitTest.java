package com.cloudcomputing.movieRetrievalWebApp.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cloudcomputing.movieRetrievalWebApp.dao.implementation.UserDAOImpl;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.repository.UserRepo;

@ExtendWith(MockitoExtension.class)
public class UserDAOImplUnitTest {

  @Mock
  private UserRepo userRepo;

  @InjectMocks
  private UserDAOImpl userDAOImpl;

  @Test
  public void testGetAllUsers() {
    // Arrange
    List<User> users = Collections.singletonList(new User("john@example.com", "password", "John", "Doe"));
    when(userRepo.findAll()).thenReturn(users);

    // Act
    List<User> result = userDAOImpl.getAllUsers();

    // Assert
    assertEquals(1, result.size());
    verify(userRepo, times(1)).findAll();
  }

  @Test
  public void testGetUserById_userExists() {
    // Arrange
    User user = new User("john@example.com", "password", "John", "Doe");
    when(userRepo.findById(1L)).thenReturn(Optional.of(user));

    // Act
    Optional<User> result = userDAOImpl.getUserById(1L);

    // Assert
    assertTrue(result.isPresent());
    assertEquals("john@example.com", result.get().getEmailAddress());
  }

  @Test
  public void testGetUserById_userDoesNotExist() {
    // Arrange
    when(userRepo.findById(1L)).thenReturn(Optional.empty());

    // Act
    Optional<User> result = userDAOImpl.getUserById(1L);

    // Assert
    assertFalse(result.isPresent());
  }

  @Test
  public void testCreateUser_userDoesNotExist() {
    // Arrange
    User newUser = new User("john@example.com", "password", "John", "Doe");
    when(userRepo.findAll()).thenReturn(Collections.emptyList());
    when(userRepo.save(any(User.class))).thenReturn(newUser);

    // Act
    User createdUser = userDAOImpl.createUser(newUser);

    // Assert
    assertEquals("john@example.com", createdUser.getEmailAddress());
    verify(userRepo, times(1)).save(any(User.class));
  }

  @Test
  public void testCreateUser_userAlreadyExists() {
    // Arrange
    User existingUser = new User("john@example.com", "password", "John", "Doe");
    when(userRepo.findAll()).thenReturn(Collections.singletonList(existingUser));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      userDAOImpl.createUser(existingUser);
    });
  }

  @Test
  public void testUpdateUser_userExists() {
    // Arrange
    User existingUser = new User("john@example.com", "password", "John", "Doe");
    User updatedUser = new User("john@example.com", "newpassword", "John", "Smith");
    when(userRepo.findAll()).thenReturn(Collections.singletonList(existingUser));
    when(userRepo.save(any(User.class))).thenReturn(updatedUser);

    // Act
    User result = userDAOImpl.updateUser("john@example.com", updatedUser);

    // Assert
    assertEquals("newpassword", result.getPassword());
    assertEquals("Smith", result.getLastName());
  }

  @Test
  public void testUpdateUser_userDoesNotExist() {
    // Arrange
    when(userRepo.findAll()).thenReturn(Collections.emptyList());
    User updatedUser = new User("john@example.com", "newpassword", "John", "Smith");

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      userDAOImpl.updateUser("nonexistent@example.com", updatedUser);
    });
  }

  @Test
  public void testDeleteUser_userExists() {
    // Arrange
    User existingUser = new User("john@example.com", "password", "John", "Doe");
    when(userRepo.findAll()).thenReturn(Collections.singletonList(existingUser));

    // Act
    userDAOImpl.deleteUser("john@example.com");

    // Assert
    verify(userRepo, times(1)).delete(existingUser);
  }

  @Test
  public void testDeleteUser_userDoesNotExist() {
    // Arrange
    when(userRepo.findAll()).thenReturn(Collections.emptyList());

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      userDAOImpl.deleteUser("nonexistent@example.com");
    });
  }
}
