package com.cloudcomputing.movieRetrievalWebApp.service;

import com.cloudcomputing.movieRetrievalWebApp.dao.UserDAO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
        MockitoAnnotations.openMocks(this);
        user = new User("user@example.com", "password", "First", "Last");
    }

    @Test
    public void testGetAllUsers() {
        when(userDAO.getAllUsers()).thenReturn(Arrays.asList(user));

        List<User> users = userService.getAllUsers();
        assertEquals(1, users.size());
        assertEquals(user, users.get(0));
    }

    @Test
    public void testGetAllUsersWhenEmpty() {
        when(userDAO.getAllUsers()).thenReturn(Collections.emptyList());

        List<User> users = userService.getAllUsers();
        assertTrue(users.isEmpty());
    }

    @Test
    public void testGetUserById() {
        when(userDAO.getUserById(1L)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.getUserById(1L);
        assertTrue(foundUser.isPresent());
        assertEquals(user, foundUser.get());
    }

    @Test
    public void testGetUserByIdNotFound() {
        when(userDAO.getUserById(2L)).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getUserById(2L);
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testGetUserByEmail() {
        when(userDAO.getAllUsers()).thenReturn(Arrays.asList(user));

        Optional<User> foundUser = userService.getUserByEmail("user@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals(user, foundUser.get());
    }

    @Test
    public void testGetUserByEmailNotFound() {
        when(userDAO.getAllUsers()).thenReturn(Collections.emptyList());

        Optional<User> foundUser = userService.getUserByEmail("user@example.com");
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testAddUser() {
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        when(userDAO.createUser(any(User.class))).thenReturn(user);

        User addedUser = userService.addUser(user);
        assertEquals(user, addedUser);
        verify(passwordEncoder).encode("password");
        verify(userDAO).createUser(any(User.class));
    }

    @Test
    public void testAddUserWithExistingEmail() {
        when(userDAO.getAllUsers()).thenReturn(Arrays.asList(user));

        User newUser = new User("user@example.com", "password", "NewFirst", "NewLast");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.addUser(newUser));
        assertEquals("User with this email already exists.", exception.getMessage());
    }

    @Test
    public void testUpdateUser() {
        when(userDAO.updateUser(anyString(), any(User.class))).thenReturn(user);
        when(passwordEncoder.encode("newPassword")).thenReturn("hashedNewPassword");

        user.setPassword("newPassword");
        User updatedUser = userService.updateUser("user@example.com", user);
        assertEquals(user, updatedUser);
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    public void testUpdateUserWithoutPassword() {
        when(userDAO.updateUser(anyString(), any(User.class))).thenReturn(user);

        user.setPassword(null);
        User updatedUser = userService.updateUser("user@example.com", user);
        assertEquals(user, updatedUser);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    public void testUpdateUserNotFound() {
        when(userDAO.updateUser(anyString(), any(User.class))).thenThrow(new IllegalArgumentException("User not found"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.updateUser("user@example.com", user));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testDeleteUser() {
        doNothing().when(userDAO).deleteUser("user@example.com");

        userService.deleteUser("user@example.com");
        verify(userDAO).deleteUser("user@example.com");
    }

    @Test
    public void testDeleteUserNotFound() {
        doThrow(new IllegalArgumentException("User not found")).when(userDAO).deleteUser("user@example.com");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.deleteUser("user@example.com"));
        assertEquals("User not found", exception.getMessage());
    }
}
