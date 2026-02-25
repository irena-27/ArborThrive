package com.blackrock.challenge.store.audit;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class InMemoryCommandAuditStore implements CommandAuditStore {
  private final CopyOnWriteArrayList<AuditEvent> events = new CopyOnWriteArrayList<>();
  public void append(String operation, Map<String, Object> metadata){ events.add(new AuditEvent(Instant.now(), operation, metadata)); }
  public List<AuditEvent> latest(int limit){ int size = events.size(); int from = Math.max(0, size - Math.max(limit,0)); return events.subList(from,size); }
}
