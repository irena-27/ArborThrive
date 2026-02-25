package com.blackrock.challenge.store.cache;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryReadCacheStore implements ReadCacheStore {
  private static class Entry { final Instant createdAt = Instant.now(); final long ttlSeconds; final Object value; Entry(Object value, long ttlSeconds){ this.value=value; this.ttlSeconds=ttlSeconds;} }
  private final Map<String, Entry> map = new ConcurrentHashMap<>();
  public Object getIfPresent(String key){ if(key==null||key.isBlank()) return null; Entry e = map.get(key); if(e==null) return null; if(Instant.now().minusSeconds(e.ttlSeconds).isAfter(e.createdAt)){ map.remove(key); return null; } return e.value; }
  public void put(String key,Object value,long ttlSeconds){ if(key==null||key.isBlank()) return; map.put(key,new Entry(value, ttlSeconds)); }
}
