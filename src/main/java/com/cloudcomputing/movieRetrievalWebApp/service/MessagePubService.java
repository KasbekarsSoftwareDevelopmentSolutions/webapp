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

  @Value("${cloud.aws.sns.topic-name}")
  private String snsTopicName;

  @Autowired
  public MessagePubService(SnsClient snsClient) {
    this.snsClient = snsClient;
  }

  /**
   * Publishes a message to the configured SNS topic.
   *
   * @param username The username of the user.
   * @param userId   The userId of the user.
   */
  public void publishMessage(String username, String userId) {
    try {
      // Create the JSON payload
      Map<String, String> messagePayload = new HashMap<>();
      messagePayload.put("username", username);
      messagePayload.put("userId", userId);

      // Convert payload to JSON
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonMessage = objectMapper.writeValueAsString(messagePayload);

      // Publish the message
      PublishRequest request = PublishRequest.builder()
        .topicArn(snsTopicName)
        .message(jsonMessage)
        .build();

      snsClient.publish(request);
    } catch (Exception e) {
      throw new RuntimeException("Failed to publish message to SNS", e);
    }
  }
}
