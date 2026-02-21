package com.blackrock.challenge.cqrs.query;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ReturnsExecutorConfig {

  /**
   * Dedicated pool for compute-heavy returns endpoints.
   * Prevents CPU-heavy calls from starving request threads (CQRS-lite scaling story).
   */
  @Bean(name = "returnsExecutor")
  public Executor returnsExecutor() {
    ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
    exec.setThreadNamePrefix("returns-");
    exec.setCorePoolSize(8);
    exec.setMaxPoolSize(16);
    exec.setQueueCapacity(200);
    exec.initialize();
    return exec;
  }
}
