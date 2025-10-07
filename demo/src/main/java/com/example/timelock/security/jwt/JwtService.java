package com.example.timelock.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
  private final String issuer;
  private final long ttlMinutes;
  private final Key key;
  private long lastExpiry = 0L;

  public JwtService(
      @Value("${jwt.issuer}") String issuer,
      @Value("${jwt.ttl-minutes}") long ttlMinutes,
      @Value("${jwt.secret}") String secret) {
    this.issuer = issuer;
    this.ttlMinutes = ttlMinutes;
    byte[] keyBytes;
    try {
        keyBytes = Decoders.BASE64.decode(secret); // try decode as Base64
    } catch (IllegalArgumentException e) {
        keyBytes = secret.getBytes(); // fallback to raw string
    }
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  public String issue(String subjectEmail, String role) {
    long now = System.currentTimeMillis();
    long exp = now + ttlMinutes * 60_000L;
    this.lastExpiry = exp;
    return Jwts.builder()
      .setSubject(subjectEmail)
      .setIssuer(issuer)
      .claim("role", role)
      .setIssuedAt(new Date(now))
      .setExpiration(new Date(exp))
      .signWith(key, SignatureAlgorithm.HS256)
      .compact();
  }

  public Key getKey() { return key; }
  public String getIssuer() { return issuer; }
  public long expiresAtMillis() { return lastExpiry; }
}
