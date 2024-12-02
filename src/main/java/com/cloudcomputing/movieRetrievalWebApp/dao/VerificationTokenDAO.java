package com.cloudcomputing.movieRetrievalWebApp.dao;

import com.cloudcomputing.movieRetrievalWebApp.model.VerificationToken;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenDAO {
  void saveVerificationToken(VerificationToken token);
  void deleteVerificationToken(Long tokenId);
  Optional<VerificationToken> getVerificationTokenByToken(UUID token);
  Optional<VerificationToken> getVerificationTokenByUserId(UUID userId);
  Optional<VerificationToken> getVerificationTokenById(Long tokenId);
}
