package com.blackrock.challenge.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommandHealthController {
  @GetMapping("/internal/command/health")
  public Map<String,Object> health(){ return Map.of("service","command-service","status","UP"); }
}
