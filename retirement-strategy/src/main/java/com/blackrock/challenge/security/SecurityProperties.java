package com.blackrock.challenge.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
  private String jwtSecret;
  private long jwtExpirationSeconds;
  private String demoUsername;
  private String demoPassword;

  public String getJwtSecret() { return jwtSecret; }
  public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }

  public long getJwtExpirationSeconds() { return jwtExpirationSeconds; }
  public void setJwtExpirationSeconds(long jwtExpirationSeconds) { this.jwtExpirationSeconds = jwtExpirationSeconds; }

  public String getDemoUsername() { return demoUsername; }
  public void setDemoUsername(String demoUsername) { this.demoUsername = demoUsername; }

  public String getDemoPassword() { return demoPassword; }
  public void setDemoPassword(String demoPassword) { this.demoPassword = demoPassword; }
}
