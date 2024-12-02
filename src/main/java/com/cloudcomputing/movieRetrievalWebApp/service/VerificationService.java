package com.cloudcomputing.movieRetrievalWebApp.service;

import com.cloudcomputing.movieRetrievalWebApp.dao.VerificationTokenDAO;
import com.cloudcomputing.movieRetrievalWebApp.model.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationService {

  private final VerificationTokenDAO verificationTokenDAO;

  @Autowired
  public VerificationService(VerificationTokenDAO verificationTokenDAO) {
    this.verificationTokenDAO = verificationTokenDAO;
  }

  /**
   * Create and save a verification token for a user.
   *
   * @param userId    The ID of the user.
   * @param userEmail The email of the user.
   * @return The created VerificationToken.
   */
  public VerificationToken createVerificationToken(UUID userId, String userEmail) {
    VerificationToken token = new VerificationToken();
    token.setToken(UUID.randomUUID()); // Generate a unique token
    token.setUserId(userId);
    token.setUserEmail(userEmail);
    token.setExpiryDate(LocalDateTime.now().plusHours(2));
    token.setVerificationFlag(false);

    verificationTokenDAO.saveVerificationToken(token);
    return token;
  }

  /**
   * Retrieve a verification token by its token value.
   *
   * @param token The token value.
   * @return Optional containing the VerificationToken if found, or empty if not.
   */
  public Optional<VerificationToken> getVerificationTokenByToken(UUID token) {
    return verificationTokenDAO.getVerificationTokenByToken(token);
  }

  /**
   * Retrieve a verification token by the associated user ID.
   *
   * @param userId The ID of the user.
   * @return Optional containing the VerificationToken if found, or empty if not.
   */
  public Optional<VerificationToken> getVerificationTokenByUserId(UUID userId) {
    return verificationTokenDAO.getVerificationTokenByUserId(userId);
  }

  /**
   * Retrieve a verification token by its token ID.
   *
   * @param tokenId The ID of the token.
   * @return Optional containing the VerificationToken if found, or empty if not.
   */
  public Optional<VerificationToken> getVerificationTokenById(Long tokenId) {
    return verificationTokenDAO.getVerificationTokenById(tokenId);
  }

  /**
   * Delete a verification token by its token ID.
   *
   * @param tokenId The ID of the token to delete.
   */
  public void deleteVerificationToken(Long tokenId) {
    verificationTokenDAO.deleteVerificationToken(tokenId);
  }

  /**
   * Check if a token is already verified.
   *
   * @param token The token value to check.
   * @return true if the token is already verified, false otherwise.
   */
  public boolean isTokenAlreadyVerified(UUID token) {
    Optional<VerificationToken> optionalToken = verificationTokenDAO.getVerificationTokenByToken(token);
    if (optionalToken.isPresent()) {
      VerificationToken verificationToken = optionalToken.get();
      return verificationToken.getVerificationFlag();
    }
    return false; // Token not found
  }

  /**
   * Verify a token by setting its verification flag to true.
   *
   * @param token The token value.
   * @return true if the token was successfully verified, false otherwise.
   */
  public boolean verifyToken(UUID token) {
    Optional<VerificationToken> optionalToken = verificationTokenDAO.getVerificationTokenByToken(token);
    if (optionalToken.isPresent()) {
      VerificationToken verificationToken = optionalToken.get();

      // Check if the token is expired
      if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
        return false; // Token is expired
      }

      // Update the verification flag
      verificationToken.setVerificationFlag(true);
      verificationTokenDAO.saveVerificationToken(verificationToken);
      return true;
    }
    return false; // Token not found
  }
}