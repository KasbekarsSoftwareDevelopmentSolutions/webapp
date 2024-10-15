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
      handleDatabaseOperations(databaseName);
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

  private void handleDatabaseOperations(String databaseName) {
    if (!tableExists("users")) {
      createUsersTable();
      seedUserData();
    } else {
      LOGGER.info("'users' table already exists in " + databaseName);
    }
    logExistingUserData();
  }

  private boolean tableExists(String tableName) {
    String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
    Integer count = jdbcTemplate.queryForObject(query, Integer.class, tableName.toUpperCase());
    return count != null && count > 0;
  }

  private void createUsersTable() {
    String createTableQuery = "CREATE TABLE users (" +
        "user_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
        "email_address VARCHAR(255) NOT NULL UNIQUE, " +
        "password VARCHAR(255) NOT NULL, " +
        "first_name VARCHAR(255) NOT NULL, " +
        "last_name VARCHAR(255), " +
        "account_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
        "account_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";
    jdbcTemplate.execute(createTableQuery);
    LOGGER.info("Table 'users' created successfully.");
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
