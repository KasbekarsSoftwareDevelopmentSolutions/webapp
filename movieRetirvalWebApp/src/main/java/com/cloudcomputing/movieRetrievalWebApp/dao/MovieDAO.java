package com.cloudcomputing.movieRetrievalWebApp.dao;

import com.cloudcomputing.movieRetrievalWebApp.model.Movie;

import java.util.List;
import java.util.Optional;

public interface MovieDAO {
    List<Movie> getAllMovies();
    Optional<Movie> getMovieById(Long id);
    Movie addMovie(Movie movie);
    Movie updateMovie(Long id, Movie movie);
    void deleteMovie(Long id);
}
