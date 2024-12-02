package com.cloudcomputing.movieRetrievalWebApp.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSSnsConfig {

  @Value("${cloud.aws.region.static}")
  private String region;

  @Value("${cloud.aws.credentials.access-key}")
  private String awsAccessKey;

  @Value("${cloud.aws.credentials.secret-key}")
  private String awsSecretKey;

  @Bean
  public SnsClient snsClient() {
    return SnsClient.builder()
      .region(Region.of(region))
      .credentialsProvider(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
      .build();
  }
}