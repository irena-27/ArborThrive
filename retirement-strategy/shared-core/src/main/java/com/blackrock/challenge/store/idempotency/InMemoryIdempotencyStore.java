package com.blackrock.challenge.store.idempotency;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryIdempotencyStore implements IdempotencyStore {
  private static class Entry { final Instant createdAt = Instant.now(); final long ttlSeconds; final Object response; Entry(Object response, long ttlSeconds){ this.response=response; this.ttlSeconds=ttlSeconds;} }
  private final Map<String, Entry> cache = new ConcurrentHashMap<>();
  public Object getIfPresent(String key){ if(key==null||key.isBlank()) return null; Entry e = cache.get(key); if(e==null) return null; if(Instant.now().minusSeconds(e.ttlSeconds).isAfter(e.createdAt)){ cache.remove(key); return null; } return e.response; }
  public void put(String key,Object response,long ttlSeconds){ if(key==null||key.isBlank()) return; cache.put(key,new Entry(response, ttlSeconds)); }
}
