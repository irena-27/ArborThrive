package com.blackrock.challenge.api;

import com.blackrock.challenge.domain.dto.*;
import com.blackrock.challenge.security.JwtService;
import com.blackrock.challenge.security.SecurityProperties;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blackrock/challenge/v1/auth")
public class AuthController {
  private final AuthenticationManager authManager;
  private final JwtService jwtService;
  private final SecurityProperties props;

  public AuthController(AuthenticationManager authManager, JwtService jwtService, SecurityProperties props) {
    this.authManager = authManager;
    this.jwtService = jwtService;
    this.props = props;
  }

  @PostMapping("/login")
  public AuthLoginResponse login(@RequestBody @Valid AuthLoginRequest req) {
    authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
    String token = jwtService.generateToken(req.username());
    return new AuthLoginResponse(token, "Bearer", props.getJwtExpirationSeconds());
  }
}
