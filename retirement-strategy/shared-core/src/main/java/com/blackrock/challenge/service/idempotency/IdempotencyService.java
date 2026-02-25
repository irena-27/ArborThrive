package com.blackrock.challenge.service.idempotency;

import com.blackrock.challenge.store.idempotency.IdempotencyStore;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {
  private final IdempotencyStore store;
  private final long ttlSeconds = 3600;
  public IdempotencyService(IdempotencyStore store){ this.store = store; }
  public Object getIfPresent(String key){ return store.getIfPresent(key); }
  public void put(String key, Object response){ store.put(key, response, ttlSeconds); }
}
