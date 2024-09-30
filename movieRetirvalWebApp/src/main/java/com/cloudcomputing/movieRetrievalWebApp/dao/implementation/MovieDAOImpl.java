package com.cloudcomputing.movieRetrievalWebApp.dao.implementation;

import com.cloudcomputing.movieRetrievalWebApp.dao.MovieDAO;
import com.cloudcomputing.movieRetrievalWebApp.model.Movie;
import com.cloudcomputing.movieRetrievalWebApp.repository.MovieRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MovieDAOImpl implements MovieDAO {

    @Autowired
    private MovieRepo movieRepo;

    @Override
    public List<Movie> getAllMovies() {
        return movieRepo.findAll();
    }

    @Override
    public Optional<Movie> getMovieById(Long id) {
        return movieRepo.findById(id);
    }

    @Override
    public Movie addMovie(Movie movie) {
        return movieRepo.save(movie);
    }

    @Override
    public Movie updateMovie(Long id, Movie movieDetails) {
        Movie movie = movieRepo.findById(id).orElseThrow();
        movie.setTitle(movieDetails.getTitle());
        movie.setGenre(movieDetails.getGenre());
        movie.setReleaseYear(movieDetails.getReleaseYear());
        return movieRepo.save(movie);
    }

    @Override
    public void deleteMovie(Long id) {
        movieRepo.deleteById(id);
    }
}
