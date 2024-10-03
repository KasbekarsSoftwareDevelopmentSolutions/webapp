package com.cloudcomputing.movieRetrievalWebApp.repository;

import com.cloudcomputing.movieRetrievalWebApp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest  // Sets up an in-memory database for testing (H2)
@ActiveProfiles("test")  // Ensure you use the test profile for in-memory DB config
class UserRepoIntegrationTest {

    @Autowired
    private UserRepo userRepo;

    private User user;

    @BeforeEach
    void setUp() {
        // Setup a test user object
        user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmailAddress("jane.doe@example.com");
        user.setPassword("password123");
    }

    // Test 1: Save and Retrieve User by ID
    @Test
    void testSaveAndFindById() {
        // Save the user to the repository
        User savedUser = userRepo.save(user);

        // Retrieve the user by ID
        Optional<User> retrievedUser = userRepo.findById(savedUser.getUserId());

        // Verify the user exists and data is as expected
        assertTrue(retrievedUser.isPresent());
        assertEquals("jane.doe@example.com", retrievedUser.get().getEmailAddress());
        assertEquals("Jane", retrievedUser.get().getFirstName());
        assertEquals("Doe", retrievedUser.get().getLastName());
    }

    // Test 2: Find User by ID - Not Found Case
    @Test
    void testFindByIdNotFound() {
        // Attempt to find a user by an invalid ID
        Optional<User> nonExistentUser = userRepo.findById(999L);

        // Assert that no user is found
        assertFalse(nonExistentUser.isPresent());
    }

    // Test 3: Delete User
    @Test
    void testDeleteUser() {
        // Save the user to the repository
        User savedUser = userRepo.save(user);

        // Delete the user
        userRepo.deleteById(savedUser.getUserId());

        // Verify the user no longer exists
        Optional<User> deletedUser = userRepo.findById(savedUser.getUserId());
        assertFalse(deletedUser.isPresent());
    }

    // Test 4: Count Users in Repository
    @Test
    void testCountUsers() {
        // Initial count should be zero
        long initialCount = userRepo.count();

        // Save a user to the repository
        userRepo.save(user);

        // Count should now be 1
        long updatedCount = userRepo.count();
        assertEquals(initialCount + 1, updatedCount);
    }

    // Test 5: Update User Information
    @Test
    void testUpdateUser() {
        // Save a user to the repository
        User savedUser = userRepo.save(user);

        // Update the user
        savedUser.setFirstName("Janet");
        userRepo.save(savedUser);

        // Retrieve the updated user
        Optional<User> updatedUser = userRepo.findById(savedUser.getUserId());

        // Verify the changes
        assertTrue(updatedUser.isPresent());
        assertEquals("Janet", updatedUser.get().getFirstName());
    }

    // Test 6: Ensure Email is Unique (Business Logic Edge Case)
    @Test
    void testSaveDuplicateEmail() {
        // Save the first user
        userRepo.save(user);

        // Attempt to save another user with the same email
        User duplicateUser = new User();
        duplicateUser.setFirstName("John");
        duplicateUser.setLastName("Doe");
        duplicateUser.setEmailAddress("jane.doe@example.com");  // Duplicate email
        duplicateUser.setPassword("password456");

        Exception exception = assertThrows(Exception.class, () -> userRepo.save(duplicateUser));
        assertNotNull(exception);  // Expecting a failure due to unique constraint violation
    }
}
