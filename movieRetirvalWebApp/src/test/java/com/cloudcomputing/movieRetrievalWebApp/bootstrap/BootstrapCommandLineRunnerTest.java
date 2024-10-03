package com.cloudcomputing.movieRetrievalWebApp.bootstrap;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;

public class BootstrapCommandLineRunnerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private BootstrapCommandLineRunner bootstrapCommandLineRunner;

    @Mock
    private PasswordEncoder passwordEncoder; // Mocked PasswordEncoder

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDatabaseConnectionSuccess() {
        // Arrange
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act
        bootstrapCommandLineRunner.run();

        // Assert
        verify(jdbcTemplate).execute("SELECT 1");
    }

    @Test
    public void testCreateUsersTableWhenNotExists() {
        // Arrange
        when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(Collections.emptyList());

        // Act
        bootstrapCommandLineRunner.run();

        // Assert
        verify(jdbcTemplate).execute(contains("CREATE TABLE users"));
    }

    @Test
    public void testSeedUserDataWhenEmpty() {
        // Arrange
        when(userRepo.findAll()).thenReturn(Collections.emptyList());
        when(passwordEncoder.encode("password1")).thenReturn("hashedPassword1");
        when(passwordEncoder.encode("password2")).thenReturn("hashedPassword2");

        // Act
        bootstrapCommandLineRunner.run(); // Calls seedUserData internally

        // Assert
        verify(userRepo, times(2)).save(any(User.class));
    }

    @Test
    public void testNoSeedingWhenDataExists() {
        // Arrange
        when(userRepo.findAll()).thenReturn(Arrays.asList(new User("existing@example.com", "hashedPassword", "First", "Last")));

        // Act
        bootstrapCommandLineRunner.run(); // Calls seedUserData internally

        // Assert
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    public void testHandleDataAccessException() {
        // Arrange
        doThrow(new DataAccessException("Database error") {}).when(jdbcTemplate).execute("SELECT 1");

        // Act
        bootstrapCommandLineRunner.run();

        // Assert
        // Verify logging behavior if logging is set up, or check that the method runs without exceptions.
    }
}
