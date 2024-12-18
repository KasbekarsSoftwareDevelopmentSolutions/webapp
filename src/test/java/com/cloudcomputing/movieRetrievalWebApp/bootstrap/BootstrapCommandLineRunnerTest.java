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
    doNothing().when(jdbcTemplate).execute("SELECT 1");

    bootstrapRunner.run();

    verify(jdbcTemplate, times(1)).execute("SELECT 1");
  }

  @Test
  void testFailedDatabaseConnectionWithSuccessfulRetry() throws Exception {
    when(jdbcTemplate.queryForObject(eq("SELECT DATABASE()"), eq(String.class))).thenReturn("testdb");

    doThrow(new DataAccessException("Connection failed") {
    })
        .doNothing()
        .when(jdbcTemplate).execute("SELECT 1");

    when(jdbcTemplate.queryForObject(
        eq("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?"),
        eq(Integer.class),
        eq("USERS"))).thenReturn(0);
    when(userRepo.findAll()).thenReturn(Collections.emptyList());

    bootstrapRunner.run();

    verify(jdbcTemplate, times(2)).execute("SELECT 1");
    verify(userRepo, times(2)).save(any(User.class));
  }

  @Test
  void testExistingUsersTable() throws Exception {
    when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("testdb");
    when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(2);
    when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(Arrays.asList(
        new User("user1@example.com", "password1", "FirstName1", "LastName1"),
        new User("user2@example.com", "password2", "FirstName2", "LastName2")));

    bootstrapRunner.run();

    verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(String.class));
    verify(jdbcTemplate, times(1)).query(anyString(), any(RowMapper.class));
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
