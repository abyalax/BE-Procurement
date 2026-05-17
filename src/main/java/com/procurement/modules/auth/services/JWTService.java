package com.procurement.modules.auth.services;

import com.procurement.common.middleware.JWTProperties;
import com.procurement.common.security.JwtUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JWTService {

  private final SecretKey secretKey;
  private final long expirationMs;

  public JWTService(JWTProperties properties) {
    String secret = properties.secret();
    if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalStateException("app.jwt.secret must be at least 32 bytes");
    }
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = properties.expirationMs();
  }

  public String generateToken(String email, Set<String> roles, Set<String> permissions) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
      .subject(email)
      .claim("roles", roles)
      .claim("permissions", permissions)
      .issuedAt(now)
      .expiration(expiryDate)
      .signWith(secretKey)
      .compact();
  }

  public boolean isTokenValid(String token) {
    return extractClaims(token).getExpiration().after(new Date());
  }

  public JwtUserPrincipal extractPrincipal(String token) {
    Claims claims = extractClaims(token);
    return new JwtUserPrincipal(
      claims.getSubject(),
      extractStringSet(claims, "roles"),
      extractStringSet(claims, "permissions")
    );
  }

  public long getExpirationMs() {
    return expirationMs;
  }

  private Claims extractClaims(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }

  private Set<String> extractStringSet(Claims claims, String key) {
    Object value = claims.get(key);
    if (value instanceof Set<?> set) {
      return set.stream().map(String::valueOf).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    if (value instanceof Iterable<?> iterable) {
      LinkedHashSet<String> result = new LinkedHashSet<>();
      for (Object item : iterable) {
        if (item != null) {
          result.add(String.valueOf(item));
        }
      }
      return result;
    }
    if (value instanceof String string && !string.isBlank()) {
      LinkedHashSet<String> result = new LinkedHashSet<>();
      for (String item : string.split(",")) {
        String trimmed = item.trim();
        if (!trimmed.isEmpty()) {
          result.add(trimmed);
        }
      }
      return result;
    }
    return Set.of();
  }
}
