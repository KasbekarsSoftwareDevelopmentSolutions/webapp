package com.cloudcomputing.movieRetrievalWebApp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class VerificationToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long tokenId;

  @Column(unique = true, nullable = false)
  private String token;

  @Column(nullable = false)
  private String userEmail;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private LocalDateTime expiryDate;

  @Column(nullable = false)
  private Boolean verificationFlag;

  // Getters and Setters
  public Long getTokenId() {
    return tokenId;
  }

  public void setTokenId(Long tokenId) {
    this.tokenId = tokenId;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public LocalDateTime getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(LocalDateTime expiryDate) {
    this.expiryDate = expiryDate;
  }

  public Boolean getVerificationFlag() {
    return verificationFlag;
  }

  public void setVerificationFlag(Boolean verificationFlag) {
    this.verificationFlag = verificationFlag;
  }
}