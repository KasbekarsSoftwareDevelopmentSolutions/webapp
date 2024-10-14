package com.cloudcomputing.movieRetrievalWebApp.dao.implementation;

import com.cloudcomputing.movieRetrievalWebApp.dao.UserDAO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDAOImpl implements UserDAO {

  @Autowired
  private UserRepo userRepo;

  @Override
  public List<User> getAllUsers() {
    return userRepo.findAll();
  }

  @Override
  public Optional<User> getUserById(Long id) {
    return userRepo.findById(id);
  }

  @Override
  public User createUser(User user) {
    try {
      if (userRepo.findAll().stream().anyMatch(u -> u.getEmailAddress().equals(user.getEmailAddress()))) {
        throw new IllegalArgumentException("User with this email already exists.");
      }
      return userRepo.save(user);
    } catch (InvalidDataAccessApiUsageException e) {
      throw new IllegalArgumentException("User with this email already exists.", e);
    }
  }

  @Override
  public User updateUser(String emailId, User updatedUserDetails) {
    Optional<User> userOptional = userRepo.findAll().stream()
        .filter(user -> user.getEmailAddress().equals(emailId))
        .findFirst();

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      user.setFirstName(updatedUserDetails.getFirstName());
      user.setLastName(updatedUserDetails.getLastName());
      user.setPassword(updatedUserDetails.getPassword()); // Encrypt password
      return userRepo.save(user);
    } else {
      throw new IllegalArgumentException("User with email " + emailId + " not found.");
    }
  }

  @Override
  public void deleteUser(String emailId) {
    Optional<User> userOptional = userRepo.findAll().stream()
        .filter(user -> user.getEmailAddress().equals(emailId))
        .findFirst();

    if (userOptional.isPresent()) {
      userRepo.delete(userOptional.get());
    } else {
      throw new IllegalArgumentException("User with email " + emailId + " not found.");
    }
  }
}
