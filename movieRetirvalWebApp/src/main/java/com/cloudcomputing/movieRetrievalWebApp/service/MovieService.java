package com.cloudcomputing.movieRetrievalWebApp.service;

import com.cloudcomputing.movieRetrievalWebApp.dao.MovieDAO;
import com.cloudcomputing.movieRetrievalWebApp.model.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieDAO movieDAO;

    public List<Movie> getAllMovies() {
        return movieDAO.getAllMovies();
    }

    public Optional<Movie> getMovieById(Long id) {
        return movieDAO.getMovieById(id);
    }

    public Movie addMovie(Movie movie) {
        return movieDAO.addMovie(movie);
    }

    public Movie updateMovie(Long id, Movie movieDetails) {
        return movieDAO.updateMovie(id, movieDetails);
    }

    public void deleteMovie(Long id) {
        movieDAO.deleteMovie(id);
    }
}
