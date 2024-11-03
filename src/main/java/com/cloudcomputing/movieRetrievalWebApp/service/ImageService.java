package com.cloudcomputing.movieRetrievalWebApp.service;

import com.cloudcomputing.movieRetrievalWebApp.dto.imagedto.ImageResponseDTO;
import com.cloudcomputing.movieRetrievalWebApp.model.Image;
import com.timgroup.statsd.StatsDClient;
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
  private S3Client s3Client;

  @Autowired
  private StatsDClient statsDClient;

  public ImageResponseDTO uploadImage(MultipartFile file, UUID userId) throws IOException {
    long startTime = System.currentTimeMillis();

    UUID imageId = UUID.randomUUID();
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
    image.setId(imageId);
    image.setUrl(bucketName + "/" + objectKey);
    image.setUploadDate(LocalDate.now());
    image.setUserId(userId);

    return new ImageResponseDTO(image.getFileName(), image.getId(), image.getUrl(), image.getUploadDate(), image.getUserId());
  }

  public ImageResponseDTO downloadImage(UUID userId) throws IOException {
    long startTime = System.currentTimeMillis();

    String prefix = userId.toString() + "/"; // Prefix for the user's images
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
      UUID imageId = UUID.fromString(s3Object.key().substring(0, s3Object.key().indexOf('/')));
      LocalDate uploadDate = LocalDate.now(); // You may need to fetch this from your database

      // Create and return ImageResponseDTO
      return new ImageResponseDTO(fileName, imageId, s3Object.key(), uploadDate, userId);
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
      statsDClient.recordExecutionTime("aws.s3.deleteImage.time", System.currentTimeMillis() - startTime);
    } else {
      statsDClient.recordExecutionTime("aws.s3.deleteImage.time", System.currentTimeMillis() - startTime);
      throw new IOException("No image found to delete for userId: " + userId);
    }
  }
}
