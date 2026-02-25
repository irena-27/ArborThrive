package com.blackrock.challenge.command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.blackrock.challenge")
public class CommandServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(CommandServiceApplication.class, args);
  }
}
