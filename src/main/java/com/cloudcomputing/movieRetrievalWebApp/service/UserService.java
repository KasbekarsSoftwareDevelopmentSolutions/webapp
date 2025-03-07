package com.cloudcomputing.movieRetrievalWebApp.service;

import com.cloudcomputing.movieRetrievalWebApp.dao.UserDAO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

  @Autowired
  private UserDAO userDAO;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public Optional<User> getUserByEmail(String email) {
    return userDAO.getAllUsers().stream()
        .filter(user -> user.getEmailAddress().equals(email))
        .findFirst();
  }

  public User addUser(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return userDAO.createUser(user);
  }

  public User updateUser(String email, User user) {
    if (user.getPassword() != null) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
    }
    return userDAO.updateUser(email, user);
  }
}
