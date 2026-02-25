package com.blackrock.challenge.store.idempotency;

public interface IdempotencyStore {
  Object getIfPresent(String key);
  void put(String key, Object response, long ttlSeconds);
}
