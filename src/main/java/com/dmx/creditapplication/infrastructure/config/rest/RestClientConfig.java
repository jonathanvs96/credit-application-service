package com.dmx.creditapplication.infrastructure.config.rest;

import com.dmx.creditapplication.infrastructure.config.properties.FrankfurterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

  private final FrankfurterProperties properties;

  @Bean
  public RestClient frankfurterRestClient(RestClient.Builder builder){
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(properties.connectTimeout());
    factory.setReadTimeout(properties.readTimeout());

    return builder
        .baseUrl(properties.url())
        .requestFactory(factory)
        .build();
  }

}
