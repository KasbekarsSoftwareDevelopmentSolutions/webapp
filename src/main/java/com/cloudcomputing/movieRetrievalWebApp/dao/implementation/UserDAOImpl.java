package com.cloudcomputing.movieRetrievalWebApp.dao.implementation;

import com.cloudcomputing.movieRetrievalWebApp.dao.UserDAO;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.cloudcomputing.movieRetrievalWebApp.repository.UserRepo;
import com.timgroup.statsd.StatsDClient;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataAccessException;

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

    try {
      return userRepo.findAll();
    } catch (DataAccessException e) {
      throw e;
    } finally {
      statsDClient.recordExecutionTime("db.query.getAllUsers.time", System.currentTimeMillis() - startTime);
    }
  }

  @Override
  public User createUser(User user) {
    long startTime = System.currentTimeMillis();

    try {
      if (userRepo.findAll().stream().anyMatch(u -> u.getEmailAddress().equals(user.getEmailAddress()))) {
        throw new EntityExistsException("User with this email already exists.");
      }

      return userRepo.save(user);
    } catch (EntityExistsException e) {
      throw e;
    } finally {
      statsDClient.recordExecutionTime("db.query.createUser.time", System.currentTimeMillis() - startTime);
    }
  }

  @Override
  public User updateUser(String emailId, User updatedUserDetails) {
    long startTime = System.currentTimeMillis();

    try {
      Optional<User> userOptional = userRepo.findAll().stream()
              .filter(user -> user.getEmailAddress().equals(emailId))
              .findFirst();

      if (userOptional.isPresent()) {
        User user = userOptional.get();
        user.setFirstName(updatedUserDetails.getFirstName());
        user.setLastName(updatedUserDetails.getLastName());
        user.setPassword(updatedUserDetails.getPassword());

        return userRepo.save(user);
      } else {
        throw new IllegalArgumentException("User with email " + emailId + " not found.");
      }
    } catch (IllegalArgumentException e) {
      throw e;
    } finally {
      statsDClient.recordExecutionTime("db.query.updateUser.time", System.currentTimeMillis() - startTime);
    }
  }
}
