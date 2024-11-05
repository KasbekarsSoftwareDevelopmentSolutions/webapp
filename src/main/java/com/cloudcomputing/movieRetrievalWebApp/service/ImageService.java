package com.cloudcomputing.movieRetrievalWebApp.service;

import com.cloudcomputing.movieRetrievalWebApp.dao.ImageDAO;
import com.cloudcomputing.movieRetrievalWebApp.dto.imagedto.ImageResponseDTO;
import com.cloudcomputing.movieRetrievalWebApp.model.Image;
import com.cloudcomputing.movieRetrievalWebApp.model.User;
import com.timgroup.statsd.StatsDClient;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageService {

  @Value("${cloud.aws.s3.bucket-name}")
  private String bucketName;

  @Autowired
  ImageDAO imageDAO;

  @Autowired
  private S3Client s3Client;

  @Autowired
  private StatsDClient statsDClient;

  public ImageResponseDTO uploadImage(MultipartFile file, UUID userId) throws IOException {
    long startTime = System.currentTimeMillis();

    String fileName = file.getOriginalFilename();

    String objectKey = userId + "/" + fileName;

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();

    s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));
    statsDClient.recordExecutionTime("aws.s3.uploadImage.time", System.currentTimeMillis() - startTime);

    Image image = new Image();
    image.setFileName(fileName);
    image.setUrl(bucketName + "/" + objectKey);
    image.setUserId(userId);

    Image savedImageDB = this.addImage_DB(image);

    return new ImageResponseDTO(savedImageDB.getFileName(), savedImageDB.getId(), savedImageDB.getUrl(), savedImageDB.getUploadDate(), savedImageDB.getUserId());
  }

  public ImageResponseDTO downloadImage(UUID userId) throws IOException {
    long startTime = System.currentTimeMillis();

    String prefix = userId.toString() + "/";
    ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(prefix)
            .build();

    ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

    statsDClient.recordExecutionTime("aws.s3.downloadImage.time", System.currentTimeMillis() - startTime);

    Optional<S3Object> imageObject = listResponse.contents().stream().findFirst();
    if (imageObject.isPresent()) {
      S3Object s3Object = imageObject.get();
      String fileName = s3Object.key().substring(s3Object.key().lastIndexOf('/') + 1);
      UUID user_Id = UUID.fromString(s3Object.key().substring(0, s3Object.key().indexOf('/')));
      Optional<Image> presentImage = this.getImageByUserId_DB(user_Id);
      if (presentImage.isEmpty()) {
        throw new EntityNotFoundException("Image with id " + userId + " does not exist");
      }
      Image image = presentImage.get();

      return new ImageResponseDTO(fileName, image.getId(), image.getUrl(), image.getUploadDate(), image.getUserId());
    } else {
      throw new IOException("Image not found for userId: " + userId);
    }
  }

  public void deleteImage(UUID userId) throws IOException {
    long startTime = System.currentTimeMillis();

    String prefix = userId.toString() + "/"; // Prefix for the user's images
    ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(prefix)
            .build();

    ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

    Optional<S3Object> imageObject = listResponse.contents().stream().findFirst();

    if (imageObject.isPresent()) {
      S3Object s3Object = imageObject.get();
      String objectKey = s3Object.key();

      // Create a request to delete the object
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
              .bucket(bucketName)
              .key(objectKey)
              .build();

      // Delete the object from S3
      s3Client.deleteObject(deleteObjectRequest);
      this.deleteImage_DB(userId);
      statsDClient.recordExecutionTime("aws.s3.deleteImage.time", System.currentTimeMillis() - startTime);
    } else {
      statsDClient.recordExecutionTime("aws.s3.deleteImage.time", System.currentTimeMillis() - startTime);
      throw new IOException("No image found to delete for userId: " + userId);
    }
  }

  public Image addImage_DB(Image image) {
    return imageDAO.createImage(image);
  }

  public Optional<Image> getImageByUserId_DB(UUID userId) {
    return imageDAO.getImageByUserId(userId);
  }

  public void deleteImage_DB(UUID userId) {
    imageDAO.deleteImage(userId);
  }
}
