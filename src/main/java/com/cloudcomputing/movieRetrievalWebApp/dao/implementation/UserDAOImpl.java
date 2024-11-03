package com.cloudcomputing.movieRetrievalWebApp.dao.implementation;

import com.cloudcomputing.movieRetrievalWebApp.dao.UserDAO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.repository.UserRepo;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDAOImpl implements UserDAO {

  @Autowired
  private UserRepo userRepo;

  @Autowired
  private StatsDClient statsDClient;

  @Override
  public List<User> getAllUsers() {
    long startTime = System.currentTimeMillis();

    List<User> users = userRepo.findAll();

    statsDClient.recordExecutionTime("db.query.getAllUsers.time", System.currentTimeMillis() - startTime);
    return users;
  }

  @Override
  public Optional<User> getUserById(Long id) {
    long startTime = System.currentTimeMillis();

    Optional<User> user = userRepo.findById(id);

    statsDClient.recordExecutionTime("db.query.getUserById.time", System.currentTimeMillis() - startTime);
    return user;
  }

  @Override
  public User createUser(User user) {
    long startTime = System.currentTimeMillis();

    try {
      if (userRepo.findAll().stream().anyMatch(u -> u.getEmailAddress().equals(user.getEmailAddress()))) {
        throw new IllegalArgumentException("User with this email already exists.");
      }
      User savedUser = userRepo.save(user);

      statsDClient.recordExecutionTime("db.query.createUser.time", System.currentTimeMillis() - startTime);
      return savedUser;
    } catch (InvalidDataAccessApiUsageException e) {
      throw new IllegalArgumentException("User with this email already exists.", e);
    }
  }

  @Override
  public User updateUser(String emailId, User updatedUserDetails) {
    long startTime = System.currentTimeMillis();

    Optional<User> userOptional = userRepo.findAll().stream()
        .filter(user -> user.getEmailAddress().equals(emailId))
        .findFirst();

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      user.setFirstName(updatedUserDetails.getFirstName());
      user.setLastName(updatedUserDetails.getLastName());
      user.setPassword(updatedUserDetails.getPassword());
      User updatedUser = userRepo.save(user);

      statsDClient.recordExecutionTime("db.query.updateUser.time", System.currentTimeMillis() - startTime);
      return updatedUser;
    } else {
      throw new IllegalArgumentException("User with email " + emailId + " not found.");
    }
  }

  @Override
  public void deleteUser(String emailId) {
    long startTime = System.currentTimeMillis();

    Optional<User> userOptional = userRepo.findAll().stream()
        .filter(user -> user.getEmailAddress().equals(emailId))
        .findFirst();

    if (userOptional.isPresent()) {
      userRepo.delete(userOptional.get());
      statsDClient.recordExecutionTime("db.query.deleteUser.time", System.currentTimeMillis() - startTime);
    } else {
      throw new IllegalArgumentException("User with email " + emailId + " not found.");
    }
  }
}
