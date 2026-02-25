package com.blackrock.challenge.store.audit;

import java.time.Instant;
import java.util.Map;

public interface CommandAuditStore {
  void append(String operation, Map<String, Object> metadata);
  record AuditEvent(Instant at, String operation, Map<String, Object> metadata) {}
}
