package com.example.rest.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
  @Value("${app.jwt.secret}")
  private String secret;

  @Value("${app.jwt.access-expiration-ms}")
  private long accessExpirationMs;

  @Value("${app.jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  private static final String TOKEN_TYPE = "type";
  private static final String ACCESS_TOKEN = "access";
  private static final String REFRESH_TOKEN = "refresh";

  public String generateAccessToken(String username) {
    return generateToken(username, ACCESS_TOKEN, accessExpirationMs);
  }

  public String generateRefreshToken(String username) {
    return generateToken(username, REFRESH_TOKEN, refreshExpirationMs);
  }

  public String extractUsername(String token) {
    return parseClaims(token).getSubject();
  }

  public boolean isAccessToken(String token) {
    return ACCESS_TOKEN.equals(extractTokenType(token));
  }

  public boolean isRefreshToken(String token) {
    return REFRESH_TOKEN.equals(extractTokenType(token));
  }

  public String extractTokenType(String token) {
    Object type = parseClaims(token).get(TOKEN_TYPE);
    return type == null ? null : type.toString();
  }

  public Claims parseClaims(String token) {
    return Jwts.parser()
      .verifyWith(getSigningKey())
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private String generateToken(String username, String type, long expirationMs) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
      .subject(username)
      .issuedAt(now)
      .expiration(expiry)
      .claim(TOKEN_TYPE, type)
      .signWith(getSigningKey())
      .compact();
  }
}
