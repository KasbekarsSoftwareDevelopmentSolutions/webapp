package com.cloudcomputing.movieRetrievalWebApp.config;

import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatsDConfig {

  @Value("${statsd.publishMessage:true}")
  private boolean publishMessage;

  @Value("${statsd.metricHost:localhost}")
  private String metricHost;

  @Value("${statsd.portNumber:8125}")
  private int portNumber;

  @Value("${statsd.prefix:csye6225}")
  private String prefix;

  @Bean
  public StatsDClient metricClient() {
    if (publishMessage) {
      return new NonBlockingStatsDClient(prefix, metricHost, portNumber);
    }
    return new NoOpStatsDClient();
  }
}
