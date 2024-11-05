package com.cloudcomputing.movieRetrievalWebApp.dao.implementation;

import com.cloudcomputing.movieRetrievalWebApp.dao.ImageDAO;
import com.cloudcomputing.movieRetrievalWebApp.model.Image;
import com.cloudcomputing.movieRetrievalWebApp.repository.ImageRepo;
import com.timgroup.statsd.StatsDClient;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ImageDAOImpl implements ImageDAO {

  @Autowired
  ImageRepo imageRepo;

  @Autowired
  StatsDClient statsDClient;

  @Override
  public List<Image> getAllImageObjects() {
    long startTime = System.currentTimeMillis();

    try {
      return imageRepo.findAll();
    } catch (DataAccessException e) {
      statsDClient.incrementCounter("db.query.getAllImageObjects.error");
      throw e;
    } finally {
      statsDClient.recordExecutionTime("db.query.getAllImageObjects.time", System.currentTimeMillis() - startTime);
    }
  }

  @Override
  public Optional<Image> getImageByUserId(UUID id) {
    long startTime = System.currentTimeMillis();

    try {
      return this.getAllImageObjects().stream()
              .filter(img -> img.getUserId().equals(id))
              .findFirst();
    } catch (DataAccessException e) {
      return Optional.empty();
    } finally {
      statsDClient.recordExecutionTime("db.query.getImageByUserId.time", System.currentTimeMillis() - startTime);
    }
  }

  @Override
  public Image createImage(Image image) {
    long startTime = System.currentTimeMillis();

    try {
      if(this.getAllImageObjects().stream().anyMatch(i -> i.getUserId().equals(image.getUserId()))) {
        throw new EntityExistsException("Image already exists");
      } else {
        return imageRepo.save(image);
      }
    } catch (EntityExistsException e) {
      throw e;
    } finally {
      statsDClient.recordExecutionTime("db.query.createImage.time", System.currentTimeMillis() - startTime);
    }
  }

  @Override
  public void deleteImage(UUID imgId) {
    long startTime = System.currentTimeMillis();

    Optional<Image> imageObj = this.getImageByUserId(imgId);
    try {
      if (imageObj.isEmpty()) {
        throw new EntityNotFoundException("Image with id " + imgId + " does not exist");
      } else {
        imageRepo.delete(imageObj.get());
      }
    } catch (EntityNotFoundException e) {
      throw e;
    } finally {
      statsDClient.recordExecutionTime("db.query.deleteImage.time", System.currentTimeMillis() - startTime);
    }
  }
}
