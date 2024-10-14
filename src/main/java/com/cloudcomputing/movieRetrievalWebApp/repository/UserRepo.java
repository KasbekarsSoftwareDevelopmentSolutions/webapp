package com.cloudcomputing.movieRetrievalWebApp.repository;

import com.cloudcomputing.movieRetrievalWebApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
}
