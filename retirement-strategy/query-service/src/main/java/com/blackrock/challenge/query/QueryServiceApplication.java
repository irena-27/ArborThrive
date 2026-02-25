package com.blackrock.challenge.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.blackrock.challenge")
public class QueryServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(QueryServiceApplication.class, args);
  }
}
