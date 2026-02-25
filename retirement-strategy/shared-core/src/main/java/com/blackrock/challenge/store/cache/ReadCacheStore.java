package com.blackrock.challenge.store.cache;

public interface ReadCacheStore {
  Object getIfPresent(String key);
  void put(String key, Object value, long ttlSeconds);
}
