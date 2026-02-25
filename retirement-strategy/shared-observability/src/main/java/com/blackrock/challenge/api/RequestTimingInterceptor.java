package com.blackrock.challenge.api;

import com.blackrock.challenge.service.RequestMetrics;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestTimingInterceptor implements HandlerInterceptor {
  private final RequestMetrics metrics;

  public RequestTimingInterceptor(RequestMetrics metrics) {
    this.metrics = metrics;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    request.setAttribute("startTime", System.nanoTime());
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    Object start = request.getAttribute("startTime");
    if (start instanceof Long st) {
      long ms = (System.nanoTime() - st) / 1_000_000;
      metrics.setLastDurationMillis(ms);
    }
  }
}
