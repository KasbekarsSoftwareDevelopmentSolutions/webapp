package com.cloudcomputing.movieRetrievalWebApp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "images")
public class Image {

  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String url;

  @Column(nullable = false)
  private LocalDate uploadDate;

  public Image() {
    this.id = UUID.randomUUID();
    this.uploadDate = LocalDate.now();
  }

  // Getters and Setters
  public String getFileName() { return fileName; }
  public void setFileName(String fileName) { this.fileName = fileName; }

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }

  public LocalDate getUploadDate() { return uploadDate; }

  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
}
