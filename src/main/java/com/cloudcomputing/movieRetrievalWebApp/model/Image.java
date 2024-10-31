package com.cloudcomputing.movieRetrievalWebApp.model;

import java.time.LocalDate;
import java.util.UUID;

public class Image {

  private String fileName;
  private UUID id;
  private String url;
  private LocalDate uploadDate;
  private UUID userId;

  // Getters and Setters
  public String getFileName() { return fileName; }
  public void setFileName(String fileName) { this.fileName = fileName; }

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }

  public LocalDate getUploadDate() { return uploadDate; }
  public void setUploadDate(LocalDate uploadDate) { this.uploadDate = uploadDate; }

  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
}
