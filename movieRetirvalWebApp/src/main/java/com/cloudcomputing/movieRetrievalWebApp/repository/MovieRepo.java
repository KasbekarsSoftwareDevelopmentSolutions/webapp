package com.cloudcomputing.movieRetrievalWebApp.repository;

import com.cloudcomputing.movieRetrievalWebApp.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepo extends JpaRepository<Movie, Long> {
}
