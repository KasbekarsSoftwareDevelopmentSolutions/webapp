package com.cloudcomputing.movieRetrievalWebApp.dao;

import com.cloudcomputing.movieRetrievalWebApp.model.VerificationToken;

import java.util.Optional;

public interface VerificationTokenDAO {
  void saveVerificationToken(VerificationToken token);
  void deleteVerificationToken(Long tokenId);
  Optional<VerificationToken> getVerificationTokenByToken(String token);
  Optional<VerificationToken> getVerificationTokenByUserId(String userId);
  Optional<VerificationToken> getVerificationTokenById(Long tokenId);
}
