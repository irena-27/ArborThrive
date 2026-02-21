package com.blackrock.challenge.service.idempotency;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {
  private static class Entry {
    final Instant createdAt = Instant.now();
    final Object response;
    Entry(Object response) { this.response = response; }
  }

  private final Map<String, Entry> cache = new ConcurrentHashMap<>();
  private final long ttlSeconds = 3600;

  public Object getIfPresent(String key) {
    if (key == null || key.isBlank()) return null;
    Entry e = cache.get(key);
    if (e == null) return null;
    if (Instant.now().minusSeconds(ttlSeconds).isAfter(e.createdAt)) {
      cache.remove(key);
      return null;
    }
    return e.response;
  }

  public void put(String key, Object response) {
    if (key == null || key.isBlank()) return;
    cache.put(key, new Entry(response));
  }
}
