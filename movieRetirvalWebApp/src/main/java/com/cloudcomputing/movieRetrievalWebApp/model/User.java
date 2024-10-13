package com.cloudcomputing.movieRetrievalWebApp.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long userId;

  @Column(nullable = false)
  private String emailAddress;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = true)
  private String lastName;

  @Column(nullable = false, updatable = false)
  private LocalDateTime accountCreated;

  @Column(nullable = false)
  private LocalDateTime accountUpdated;

  public User() {
    this.accountCreated = LocalDateTime.now();
    this.accountUpdated = LocalDateTime.now();
  }

  public User(String emailAddress, String password, String firstName, String lastName) {
    this.emailAddress = emailAddress;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.accountCreated = LocalDateTime.now();
    this.accountUpdated = LocalDateTime.now();
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public LocalDateTime getAccountCreated() {
    return accountCreated;
  }

  public LocalDateTime getAccountUpdated() {
    return accountUpdated;
  }

  @Override
  public String toString() {
    return "User{" +
        "userId=" + userId +
        ", emailAddress='" + emailAddress + '\'' +
        ", password='####$$$$'" + // Placeholder for the password
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", accountCreated=" + accountCreated +
        ", accountUpdated=" + accountUpdated +
        '}';
  }
}
