package com.cloudcomputing.movieRetrievalWebApp.dao;

import com.cloudcomputing.movieRetrievalWebApp.model.Image;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageDAO {

  List<Image> getAllImageObjects();

  Optional<Image> getImageByUserId(UUID id);

  Image createImage(Image image);

  void deleteImage(UUID imgId);
}
