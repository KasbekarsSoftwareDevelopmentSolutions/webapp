package com.cloudcomputing.movieRetrievalWebApp.dao;

import com.cloudcomputing.movieRetrievalWebApp.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    User createUser(User user);
    User updateUser(String emailId, User user);
    void deleteUser(String emailId);
}
