package com.blackrock.challenge.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecurityProperties props;

  public JwtService(SecurityProperties props) {
    this.props = props;
  }

  public String generateToken(String subject) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + props.getJwtExpirationSeconds() * 1000L);
    return Jwts.builder()
        .subject(subject)
        .issuedAt(now)
        .expiration(exp)
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String extractSubject(String token) {
    return Jwts.parser()
        .verifyWith(key())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  public boolean isValid(String token) {
    try {
      extractSubject(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private SecretKey key() {
    // If secret isn't base64, we still accept it by padding/encoding.
    byte[] keyBytes;
    try {
      keyBytes = Decoders.BASE64.decode(props.getJwtSecret());
    } catch (Exception e) {
      keyBytes = props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    return Keys.hmacShaKeyFor(padToMin(keyBytes, 32));
  }

  private byte[] padToMin(byte[] in, int min) {
    if (in.length >= min) return in;
    byte[] out = new byte[min];
    System.arraycopy(in, 0, out, 0, in.length);
    for (int i = in.length; i < min; i++) out[i] = (byte) 1;
    return out;
  }
}
