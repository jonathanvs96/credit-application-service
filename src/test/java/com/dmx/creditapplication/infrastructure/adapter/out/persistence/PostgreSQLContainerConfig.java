package com.dmx.creditapplication.infrastructure.adapter.out.persistence;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgreSQLContainerConfig {

  static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("credit_db")
          .withUsername("test")
          .withPassword("test");

  static {
    POSTGRE_SQL_CONTAINER.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
  }
}
