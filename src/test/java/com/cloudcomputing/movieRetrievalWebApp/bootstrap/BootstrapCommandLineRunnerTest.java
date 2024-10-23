package com.cloudcomputing.movieRetrievalWebApp.bootstrap;

import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BootstrapCommandLineRunnerTest {

  private BootstrapCommandLineRunner bootstrapRunner;

  @Mock
  private JdbcTemplate jdbcTemplate;

  @Mock
  private UserRepo userRepo;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    bootstrapRunner = new BootstrapCommandLineRunner(jdbcTemplate, userRepo);
  }

  @Test
  void testSuccessfulDatabaseConnection() throws Exception {
    // Mock the behavior of the jdbcTemplate to simulate a successful connection
    doNothing().when(jdbcTemplate).execute("SELECT 1");

    // Call the public method (like run()) that internally calls
    // checkDatabaseConnectionWithRetry()
    bootstrapRunner.run();

    // Verify that the jdbcTemplate.execute("SELECT 1") was called exactly once
    // (since the connection should succeed on the first attempt)
    verify(jdbcTemplate, times(1)).execute("SELECT 1");
  }

  @Test
  void testFailedDatabaseConnectionWithSuccessfulRetry() throws Exception {
    when(jdbcTemplate.queryForObject(eq("SELECT DATABASE()"), eq(String.class))).thenReturn("testdb");

    // Simulate failed connection followed by successful retry
    doThrow(new DataAccessException("Connection failed") {
    })
        .doNothing()
        .when(jdbcTemplate).execute("SELECT 1");

    when(jdbcTemplate.queryForObject(
        eq("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?"),
        eq(Integer.class),
        eq("USERS"))).thenReturn(0); // Simulate table doesn't exist

    when(userRepo.findAll()).thenReturn(Collections.emptyList());

    bootstrapRunner.run();

    // Verify that execute was called twice (once for failed attempt, once for
    // successful retry)
    verify(jdbcTemplate, times(2)).execute("SELECT 1");

    // Verify table creation and user seeding
    verify(userRepo, times(2)).save(any(User.class));
  }

  @Test
  void testExistingUsersTable() throws Exception {
    // Simulate the existence of the "testdb" database
    when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("testdb");

    // Simulate the count of users in the users table (e.g., 2 existing users)
    when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(2);

    // Simulate the retrieval of users from the "users" table
    when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(Arrays.asList(
        new User("user1@example.com", "password1", "FirstName1", "LastName1"),
        new User("user2@example.com", "password2", "FirstName2", "LastName2")));

    // Execute the BootstrapCommandLineRunner
    bootstrapRunner.run();

    // Verify that the database name was retrieved once
    verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(String.class));

    // Verify that the user data was retrieved once from the "users" table
    verify(jdbcTemplate, times(1)).query(anyString(), any(RowMapper.class));

    // Verify that no users were saved, since the users table already existed and
    // contained data
    verify(userRepo, never()).save(any(User.class));
  }

  @Test
  void testNonExistingUsersTable() throws Exception {
    when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("testdb");
    when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);
    when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(Collections.emptyList());
    when(userRepo.findAll()).thenReturn(Collections.emptyList());

    bootstrapRunner.run();

    verify(userRepo, times(2)).findAll();
    verify(userRepo, times(4)).save(any(User.class));
  }

  @Test
  void testEmptyUsersTable() throws Exception {
    when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("testdb");
    when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(Collections.singletonList("users"));
    when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(Collections.emptyList());
    when(userRepo.findAll()).thenReturn(Collections.emptyList());

    bootstrapRunner.run();

    verify(jdbcTemplate, times(1)).query(anyString(), any(RowMapper.class));
    verify(userRepo, times(1)).findAll();
    verify(userRepo, times(2)).save(any(User.class));
  }

  @Test
  void testInterruptedRetryAttempt() throws Exception {
    when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("testdb");
    doThrow(new DataAccessException("Connection failed") {
    })
        .when(jdbcTemplate).execute("SELECT 1");

    Thread.currentThread().interrupt();

    bootstrapRunner.run();

    assertTrue(Thread.interrupted());
    verify(jdbcTemplate, atLeastOnce()).execute("SELECT 1");
    verify(jdbcTemplate, never()).queryForList(anyString(), eq(String.class));
  }

  private static class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new User(
          rs.getString("email_address"),
          rs.getString("password"),
          rs.getString("first_name"),
          rs.getString("last_name"));
    }
  }
}
