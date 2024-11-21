package com.cloudcomputing.movieRetrievalWebApp.dao.implementation;

import com.cloudcomputing.movieRetrievalWebApp.dao.VerificationTokenDAO;
import com.cloudcomputing.movieRetrievalWebApp.model.VerificationToken;
import com.cloudcomputing.movieRetrievalWebApp.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class VerificationTokenDAOImpl implements VerificationTokenDAO {

  @Autowired
  private VerificationTokenRepository repository;

  @Override
  public void saveVerificationToken(VerificationToken token) {
    repository.save(token);
  }

  @Override
  public void deleteVerificationToken(Long tokenId) {
    repository.deleteById(tokenId);
  }

  @Override
  public Optional<VerificationToken> getVerificationTokenByToken(String token) {
    return repository.findByToken(token);
  }

  @Override
  public Optional<VerificationToken> getVerificationTokenByUserId(String userId) {
    return repository.findByUserId(userId);
  }

  @Override
  public Optional<VerificationToken> getVerificationTokenById(Long tokenId) {
    return repository.findById(tokenId);
  }
}
