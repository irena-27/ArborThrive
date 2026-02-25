package com.blackrock.challenge.service;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class RequestMetrics {
  private final AtomicLong lastDurationMillis = new AtomicLong(0);

  public void setLastDurationMillis(long ms) {
    lastDurationMillis.set(ms);
  }

  public long getLastDurationMillis() {
    return lastDurationMillis.get();
  }
}
