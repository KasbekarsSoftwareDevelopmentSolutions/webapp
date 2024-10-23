package com.cloudcomputing.movieRetrievalWebApp.bootstrap;

import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

@Component
public class BootstrapCommandLineRunner implements CommandLineRunner {

  private static final Logger LOGGER = Logger.getLogger(BootstrapCommandLineRunner.class.getName());
  private static final int MAX_RETRY_ATTEMPTS = 3;
  private static final long RETRY_DELAY_MS = 1000;

  private final JdbcTemplate jdbcTemplate;
  private final UserRepo userRepo;

  @Autowired
  public BootstrapCommandLineRunner(JdbcTemplate jdbcTemplate, UserRepo userRepo) {
    this.jdbcTemplate = jdbcTemplate;
    this.userRepo = userRepo;
  }

  @Override
  public void run(String... args) {
    String databaseName = getDatabaseName();
    if (checkDatabaseConnectionWithRetry(databaseName)) {
      handleDatabaseOperations();
    } else {
      LOGGER.severe("Failed to connect to the database: " + databaseName);
    }
  }

  private boolean checkDatabaseConnectionWithRetry(String databaseName) {
    for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
      try {
        jdbcTemplate.execute("SELECT 1");
        LOGGER.info("Database connection to " + databaseName + " is successful!");
        return true;
      } catch (DataAccessException e) {
        LOGGER.warning("Attempt " + attempt + " failed: " + e.getMessage());
        if (attempt < MAX_RETRY_ATTEMPTS) {
          try {
            Thread.sleep(RETRY_DELAY_MS);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
          }
        }
      }
    }
    return false;
  }

  private void handleDatabaseOperations() {
    if (jdbcTemplate == null) {
      throw new IllegalStateException("JdbcTemplate is not initialized");
    }
    try {
      Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
      if (userCount != null && userCount == 0) {
        seedUserData();
      }
      logExistingUserData();
    } catch (DataAccessException e) {
      LOGGER.warning("Error accessing the database" + e.toString());
    }
  }

  private void seedUserData() {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    List<User> users = userRepo.findAll();

    if (users.isEmpty()) {
      User user1 = new User("user1@example.com", passwordEncoder.encode("password1"), "FirstName1", "LastName1");
      User user2 = new User("user2@example.com", passwordEncoder.encode("password2"), "FirstName2", "LastName2");
      userRepo.save(user1);
      userRepo.save(user2);
      LOGGER.info("Seed data insertion successful.");
    } else {
      LOGGER.info("'users' table already contains data.");
    }
  }

  private void logExistingUserData() {
    String query = "SELECT * FROM users LIMIT 5";
    List<User> users = jdbcTemplate.query(query, (rs, rowNum) -> new User(
        rs.getString("email_address"),
        rs.getString("password"),
        rs.getString("first_name"),
        rs.getString("last_name")));

    if (users.isEmpty()) {
      LOGGER.info("'users' table is empty.");
      seedUserData();
    } else {
      LOGGER.info("Existing data in 'users' table (first 5 rows):");
      for (User user : users) {
        LOGGER.info(user.toString());
      }
    }
  }

  private String getDatabaseName() {
    String query = "SELECT DATABASE()";
    return jdbcTemplate.queryForObject(query, String.class);
  }
}
