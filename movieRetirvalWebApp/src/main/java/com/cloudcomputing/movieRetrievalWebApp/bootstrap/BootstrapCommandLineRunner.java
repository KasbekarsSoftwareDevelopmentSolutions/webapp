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

    private final JdbcTemplate jdbcTemplate;
    private final UserRepo userRepo;

    @Autowired
    public BootstrapCommandLineRunner(JdbcTemplate jdbcTemplate, UserRepo userRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepo = userRepo;
    }

    @Override
    public void run(String... args) {
        // PasswordEncoder will be passed as a parameter
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // Or use dependency injection if needed

        try {
            // Check if DB connection is successful
            jdbcTemplate.execute("SELECT 1");
            LOGGER.info("Database connection successful!");

            // List all tables in the current database
            List<String> existingTables = listExistingTables();
            LOGGER.info("Existing tables in the database: " + existingTables);

            // Check if 'users' table exists, create if not
            if (!existingTables.contains("users")) {
                createUsersTable();
                seedUserData(passwordEncoder); // Pass the PasswordEncoder here
            } else {
                LOGGER.info("'users' table already exists.");
                // Log existing data in the 'users' table
                logExistingUserData();
            }
        } catch (DataAccessException e) {
            LOGGER.severe("Error connecting to the database: " + e.getMessage());
        }
    }

    /**
     * This method lists all the existing tables in the current database.
     *
     * @return List of table names
     */
    private List<String> listExistingTables() {
        String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()";
        return jdbcTemplate.queryForList(query, String.class);
    }

    /**
     * Creates the 'users' table if it does not exist.
     */
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

    /**
     * Seeds initial data into the 'users' table.
     */
    private void seedUserData(PasswordEncoder passwordEncoder) {
        List<User> users = userRepo.findAll();

        // Seed data only if table is empty
        if (users.isEmpty()) {
            User user1 = new User("user1@example.com", passwordEncoder.encode("password1"), "FirstName1", "LastName1");
            User user2 = new User("user2@example.com", passwordEncoder.encode("password2"), "FirstName2", "LastName2");
            userRepo.save(user1);
            userRepo.save(user2);
            LOGGER.info("Seed data added to 'users' table.");
        } else {
            LOGGER.info("'users' table already contains data.");
        }
    }

    /**
     * Logs existing data in the 'users' table, showing the first 5 rows.
     */
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
        } else {
            LOGGER.info("Existing data in 'users' table (first 5 rows):");
            for (User user : users) {
                LOGGER.info(user.toString());
            }
        }
    }
}
