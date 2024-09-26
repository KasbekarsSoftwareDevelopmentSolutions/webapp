package com.cloudcomputing.movieRetrievalWebApp.service;

import com.cloudcomputing.movieRetrievalWebApp.repository.MovieRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

    @Autowired
    private MovieRepo movieRepo;

//    public List<Movie> getAllMovies() {
//        return movieRepo.findAll();
//    }
//
//    public Optional<Movie> getMovieById(Long id) {
//        return movieRepo.findById(id);
//    }
//
//    public Movie addMovie(Movie movie) {
//        return movieRepo.save(movie);
//    }
//
//    public Movie updateMovie(Long id, Movie movieDetails) {
//        Movie movie = movieRepo.findById(id).orElseThrow();
//        movie.setTitle(movieDetails.getTitle());
//        movie.setGenre(movieDetails.getGenre());
//        movie.setReleaseYear(movieDetails.getReleaseYear());
//        return movieRepo.save(movie);
//    }
//
//    public void deleteMovie(Long id) {
//        movieRepo.deleteById(id);
//    }
}
