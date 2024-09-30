package com.cloudcomputing.movieRetrievalWebApp.bootstrap;

import com.cloudcomputing.movieRetrievalWebApp.model.Movie;
import com.cloudcomputing.movieRetrievalWebApp.repository.MovieRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class BootstrapCommandLineRunner implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(BootstrapCommandLineRunner.class.getName());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MovieRepo movieRepo;

    @Override
    public void run(String... args) {
        try {
            // Check if DB connection is successful
            jdbcTemplate.execute("SELECT 1");
            LOGGER.info("Database connection successful!");

            // List all tables in the current database
            List<String> existingTables = listExistingTables();
            LOGGER.info("Existing tables in the database: " + existingTables);

            // Check if 'movies' table exists, create if not
            if (!existingTables.contains("movies")) {
                createMoviesTable();
            }

            // Seed the database with initial data
            seedData();
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
        List<String> tables = jdbcTemplate.queryForList(query, String.class);
        return tables;
    }

    /**
     * Creates the 'movies' table if it does not exist.
     */
    private void createMoviesTable() {
        String createTableQuery = "CREATE TABLE movies (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "genre VARCHAR(255) NOT NULL, " +
                "release_year INT NOT NULL)";
        jdbcTemplate.execute(createTableQuery);
        LOGGER.info("Table 'movies' created successfully.");
    }

    /**
     * Seeds initial data into the 'movies' table.
     */
    private void seedData() {
        List<Movie> movies = movieRepo.findAll();

        // Seed data only if table is empty
        if (movies.isEmpty()) {
            Movie movie1 = new Movie("The Shawshank Redemption", "Drama", 1994);
            Movie movie2 = new Movie("The Godfather", "Crime", 1972);
            movieRepo.save(movie1);
            movieRepo.save(movie2);
            LOGGER.info("Seed data added to 'movies' table.");
        } else {
            LOGGER.info("'movies' table already contains data.");
        }
    }
}
