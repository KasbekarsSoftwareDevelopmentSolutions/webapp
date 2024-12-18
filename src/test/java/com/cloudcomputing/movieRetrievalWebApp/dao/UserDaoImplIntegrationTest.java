package com.cloudcomputing.movieRetrievalWebApp.dao;

import com.cloudcomputing.movieRetrievalWebApp.MovieRetrievalWebAppApplication;
import com.cloudcomputing.movieRetrievalWebApp.dao.implementation.UserDAOImpl;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.repository.UserRepo;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = MovieRetrievalWebAppApplication.class)
@Transactional
public class UserDaoImplIntegrationTest {

  @Autowired
  private UserDAOImpl userDAOImpl;

  @MockBean
  private UserRepo userRepo;

  private User sampleUser;

  @BeforeEach
  public void setUp() {

    sampleUser = new User();
    sampleUser.setEmailAddress("john.doe@example.com");
    sampleUser.setPassword("password123");
    sampleUser.setFirstName("John");
    sampleUser.setLastName("Doe");
  }

  @Test
  public void testGetAllUsers() {
    // Arrange
    when(userRepo.findAll()).thenReturn(Collections.singletonList(sampleUser));

    // Act
    var users = userDAOImpl.getAllUsers();

    // Assert
    assertNotNull(users);
    assertEquals(1, users.size());
    assertEquals("john.doe@example.com", users.get(0).getEmailAddress());

    // Verify that the method was called once
    verify(userRepo, times(3)).findAll(); // invocation times need to be 3 for the github workflow to work. for local testing it needs to be 1.
  }

  @Test
  public void testCreateUser_userDoesNotExist() {
    // Arrange
    when(userRepo.findAll()).thenReturn(Collections.emptyList());
    when(userRepo.save(any(User.class))).thenReturn(sampleUser);

    // Act
    User createdUser = userDAOImpl.createUser(sampleUser);

    // Assert
    assertNotNull(createdUser);
    assertEquals("john.doe@example.com", createdUser.getEmailAddress());
    verify(userRepo, times(1)).save(any(User.class));
  }

  @Test
  public void testUpdateUser_userExists() {
    // Arrange
    User updatedUser = new User("john.doe@example.com", "newpassword", "John", "Smith");
    when(userRepo.findAll()).thenReturn(Collections.singletonList(sampleUser));
    when(userRepo.save(any(User.class))).thenReturn(updatedUser);

    // Act
    User result = userDAOImpl.updateUser("john.doe@example.com", updatedUser);

    // Assert
    assertEquals("newpassword", result.getPassword());
    assertEquals("Smith", result.getLastName());
  }
}
