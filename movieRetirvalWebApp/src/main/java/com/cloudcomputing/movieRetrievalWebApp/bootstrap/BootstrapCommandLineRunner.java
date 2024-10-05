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
    private static final int MAX_RETRY_TIME_MS = 5000;
    private static final int RETRY_INTERVAL_MS = 1000;
    private final JdbcTemplate jdbcTemplate;
    private final UserRepo userRepo;

    @Autowired
    public BootstrapCommandLineRunner(JdbcTemplate jdbcTemplate, UserRepo userRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepo = userRepo;
    }

    @Override
    public void run(String... args) {
        String databaseName = getDatabaseName(); // Get the DB name
        if (checkDatabaseConnection(databaseName)) {
            handleDatabaseOperations(databaseName);
        } else {
            LOGGER.info("Trying to reconnect to the database: " + databaseName);
            if (attemptReconnection(databaseName)) {
                handleDatabaseOperations(databaseName);
            } else {
                LOGGER.severe("Trying to reconnect to the database " + databaseName + " failed.");
            }
        }
    }

    private boolean checkDatabaseConnection(String databaseName) {
        try {
            jdbcTemplate.execute("SELECT 1");
            LOGGER.info("Database connection to " + databaseName + " is successful!");
            return true;
        } catch (DataAccessException e) {
            LOGGER.severe("Database connection to " + databaseName + " is failed: " + e.getMessage());
            return false;
        }
    }

    private boolean attemptReconnection(String databaseName) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < MAX_RETRY_TIME_MS) {
            try {
                Thread.sleep(RETRY_INTERVAL_MS);  // Wait before retrying
                jdbcTemplate.execute("SELECT 1");
                LOGGER.info("Database connection to " + databaseName + " is successful after retrying!");
                return true;
            } catch (DataAccessException | InterruptedException e) {
                LOGGER.warning("Retry failed: " + e.getMessage());
            }
        }
        return false;
    }

    private void handleDatabaseOperations(String databaseName) {
        List<String> existingTables = listExistingTables();
        if (!existingTables.isEmpty()) {
            LOGGER.info("Existing tables in the database: " + existingTables);
        } else {
            LOGGER.info("No tables found in the database.");
        }

        if (existingTables.contains("users")) {
            LOGGER.info("'users' table exists in the " + databaseName + ".");
            logExistingUserData();
        } else {
            LOGGER.info("'users' table not found in the " + databaseName + ".");
            createUsersTable();
            seedUserData();
            logExistingUserData();
        }
    }

    private List<String> listExistingTables() {
        String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()";
        return jdbcTemplate.queryForList(query, String.class);
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
                rs.getString("last_name")
        ));

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
