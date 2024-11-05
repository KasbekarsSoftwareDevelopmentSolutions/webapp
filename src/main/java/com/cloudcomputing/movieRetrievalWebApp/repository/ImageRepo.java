package com.cloudcomputing.movieRetrievalWebApp.repository;

import com.cloudcomputing.movieRetrievalWebApp.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepo  extends JpaRepository<Image, Long> {
}
