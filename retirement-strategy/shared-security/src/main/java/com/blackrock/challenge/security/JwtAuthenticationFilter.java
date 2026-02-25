package com.blackrock.challenge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected boolean shouldNotFilterAsyncDispatch() {
    // Controllers return CompletionStage which triggers an async dispatch after the future completes.
    // OncePerRequestFilter skips async dispatch by default; that would skip JWT auth on the async
    // dispatch and can result in a 401 even though the controller computed a response.
    return false;
  }

  @Override
  protected boolean shouldNotFilterErrorDispatch() {
    return false;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String auth = request.getHeader("Authorization");

    // No bearer token -> continue; Spring Security will handle unauthenticated response.
    if (auth == null || !auth.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = auth.substring(7).trim();

    // If token is present but invalid, fail fast with 401 (prevents confusing Basic challenges).
    if (!jwtService.isValid(token)) {
      log.warn("Invalid JWT for {} {}", request.getMethod(), request.getRequestURI());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String username = jwtService.extractSubject(token);

    var existing = SecurityContextHolder.getContext().getAuthentication();
    // If already authenticated with a real user, keep it. If anonymous, replace with JWT user.
    if (existing == null || existing instanceof AnonymousAuthenticationToken) {
      var user = userDetailsService.loadUserByUsername(username);
      var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      log.debug("JWT authenticated subject={} for {} {}", username, request.getMethod(), request.getRequestURI());
    }

    filterChain.doFilter(request, response);
  }
}
