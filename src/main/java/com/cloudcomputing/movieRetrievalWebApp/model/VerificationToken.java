package com.cloudcomputing.movieRetrievalWebApp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class VerificationToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long tokenId;

  @Column(unique = true, nullable = false)
  private UUID token;

  @Column(nullable = false)
  private String userEmail;

  @Column(nullable = false)
  private UUID userId;

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

  public UUID getToken() {
    return token;
  }

  public void setToken(UUID token) {
    this.token = token;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
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