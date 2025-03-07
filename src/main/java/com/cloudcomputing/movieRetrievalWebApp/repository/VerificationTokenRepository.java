package com.cloudcomputing.movieRetrievalWebApp.repository;


import com.cloudcomputing.movieRetrievalWebApp.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

  Optional<VerificationToken> findByToken(UUID token);
  Optional<VerificationToken> findByUserId(UUID userId);
}
