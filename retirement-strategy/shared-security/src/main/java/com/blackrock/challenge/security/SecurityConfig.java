package com.blackrock.challenge.security;

import com.blackrock.challenge.api.CorrelationIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
public class SecurityConfig {

  @Bean
  public UserDetailsService userDetailsService(SecurityProperties props, PasswordEncoder encoder) {
    UserDetails demo = User.withUsername(props.getDemoUsername())
        .password(encoder.encode(props.getDemoPassword()))
        .roles("USER")
        .build();
    return new InMemoryUserDetailsManager(demo);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtFilter,
      CorrelationIdFilter correlationFilter
  ) throws Exception {

    http.csrf(AbstractHttpConfigurer::disable);

    http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/blackrock/challenge/v1/auth/**").permitAll()
        .anyRequest().authenticated()
    );
    // Ensure our filters run BEFORE authorization decisions
    http.addFilterBefore(correlationFilter, AuthorizationFilter.class);
    http.addFilterBefore(jwtFilter, AuthorizationFilter.class);

    // IMPORTANT: disable Basic/Form auth so it doesn't override/compete with Bearer JWT
    http.httpBasic(AbstractHttpConfigurer::disable);
    http.formLogin(AbstractHttpConfigurer::disable);

    // Return plain 401 (no WWW-Authenticate: Basic)
    http.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

    return http.build();
  }
}
