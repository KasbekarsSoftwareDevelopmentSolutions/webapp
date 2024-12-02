package com.cloudcomputing.movieRetrievalWebApp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessagePubService {

  private final SnsClient snsClient;

  // Update this to use the correct property
  @Value("${cloud.aws.sns.topic-arn}")
  private String snsTopicArn;

  @Autowired
  public MessagePubService(SnsClient snsClient) {
    this.snsClient = snsClient;
  }

  /**
   * Publishes a message to the configured SNS topic.
   *
   * @param userEmailAddress The email address of the user.
   * @param userFirstName The first name of the user.
   * @param userId The ID of the user.
   * @param token The verification token.
   */
  public void publishMessage(String userEmailAddress, String userFirstName, String userId, String token) {
    try {
      // Create the JSON payload
      Map<String, String> messagePayload = new HashMap<>();
      messagePayload.put("userEmailAddress", userEmailAddress);
      messagePayload.put("userFirstName", userFirstName);
      messagePayload.put("userId", userId);
      messagePayload.put("token", token);

      // Convert payload to JSON
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonMessage = objectMapper.writeValueAsString(messagePayload);

      // Publish the message to the SNS topic using the ARN
      PublishRequest request = PublishRequest.builder()
        .topicArn(snsTopicArn)  // Use the ARN directly from the properties
        .message(jsonMessage)
        .build();

      snsClient.publish(request);
    } catch (Exception e) {
      throw new RuntimeException("Failed to publish message to SNS", e);
    }
  }
}
