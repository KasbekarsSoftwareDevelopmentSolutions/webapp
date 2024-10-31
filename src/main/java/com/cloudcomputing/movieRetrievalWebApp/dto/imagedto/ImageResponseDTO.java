package com.cloudcomputing.movieRetrievalWebApp.dto.imagedto;

import java.time.LocalDate;
import java.util.UUID;

public class ImageResponseDTO {

  private String fileName;
  private UUID id;
  private String url;
  private LocalDate uploadDate;
  private UUID userId;

  // Constructor, Getters, and Setters
  public ImageResponseDTO(String fileName, UUID id, String url, LocalDate uploadDate, UUID userId) {
    this.fileName = fileName;
    this.id = id;
    this.url = url;
    this.uploadDate = uploadDate;
    this.userId = userId;
  }

  public String getFileName() { return fileName; }
  public UUID getId() { return id; }
  public String getUrl() { return url; }
  public LocalDate getUploadDate() { return uploadDate; }
  public UUID getUserId() { return userId; }
}
