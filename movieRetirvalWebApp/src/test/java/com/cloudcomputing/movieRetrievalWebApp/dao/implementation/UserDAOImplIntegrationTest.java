package com.cloudcomputing.movieRetrievalWebApp.dao.implementation;

import com.cloudcomputing.movieRetrievalWebApp.dao.UserDAO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")  // Use the test profile for in-memory database
class UserDAOImplIntegrationTest {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmailAddress("john.doe@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
    }

    // Test case for createUser(User user)
    @Test
    void testCreateUser() {
        // Test successful user creation
        User createdUser = userDAO.createUser(testUser);
        assertNotNull(createdUser.getUserId());
        assertEquals(testUser.getEmailAddress(), createdUser.getEmailAddress());

        // Check that the password is encoded
        assertNotEquals("password123", createdUser.getPassword());
        assertTrue(passwordEncoder.matches("password123", createdUser.getPassword()));
    }

    @Test
    void testCreateUserDuplicateEmail() {
        // First, create the user
        userDAO.createUser(testUser);

        // Try to create another user with the same email
        User duplicateUser = new User();
        duplicateUser.setFirstName("Jane");
        duplicateUser.setLastName("Doe");
        duplicateUser.setEmailAddress("john.doe@example.com");  // Duplicate email
        duplicateUser.setPassword("password456");

        assertThrows(IllegalArgumentException.class, () -> userDAO.createUser(duplicateUser));
    }

    // Test case for getUserById(Long id)
    @Test
    void testGetUserById() {
        // Save the user and get the ID
        User createdUser = userDAO.createUser(testUser);

        // Retrieve the user by ID
        Optional<User> foundUser = userDAO.getUserById(createdUser.getUserId());
        assertTrue(foundUser.isPresent());
        assertEquals("John", foundUser.get().getFirstName());
    }

    @Test
    void testGetUserByIdNotFound() {
        // Try to find a user that doesn't exist
        Optional<User> foundUser = userDAO.getUserById(999L);
        assertFalse(foundUser.isPresent());
    }

    // Test case for updateUser(String emailId, User updatedUserDetails)
    @Test
    void testUpdateUser() {
        // First, create and save the user
        userDAO.createUser(testUser);

        // Update user details
        testUser.setFirstName("UpdatedName");
        testUser.setPassword(passwordEncoder.encode("newPassword"));

        User updatedUser = userDAO.updateUser(testUser.getEmailAddress(), testUser);

        assertEquals("UpdatedName", updatedUser.getFirstName());
        assertTrue(passwordEncoder.matches("newPassword", updatedUser.getPassword()));
    }

    @Test
    void testUpdateUserNotFound() {
        // Try to update a non-existing user
        User nonExistentUser = new User();
        nonExistentUser.setFirstName("NonExistent");
        nonExistentUser.setEmailAddress("nonexistent@example.com");

        assertThrows(IllegalArgumentException.class, () -> userDAO.updateUser("nonexistent@example.com", nonExistentUser));
    }

    // Test case for deleteUser(String emailId)
    @Test
    void testDeleteUser() {
        // First, create and save the user
        userDAO.createUser(testUser);

        // Delete the user
        userDAO.deleteUser(testUser.getEmailAddress());

        // Verify that the user no longer exists
        Optional<User> deletedUser = userRepo.findAll().stream()
                .filter(user -> user.getEmailAddress().equals(testUser.getEmailAddress()))
                .findFirst();

        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testDeleteUserNotFound() {
        // Try to delete a user that doesn't exist
        assertThrows(IllegalArgumentException.class, () -> userDAO.deleteUser("nonexistent@example.com"));
    }

    // Test case for getAllUsers()
    @Test
    void testGetAllUsers() {
        // Initially the repo is empty
        assertTrue(userDAO.getAllUsers().isEmpty());

        // Save a user
        userDAO.createUser(testUser);

        // Verify that the user is in the list
        assertEquals(1, userDAO.getAllUsers().size());
    }
}
