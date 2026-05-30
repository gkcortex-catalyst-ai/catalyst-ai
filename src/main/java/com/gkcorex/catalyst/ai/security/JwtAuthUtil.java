package com.gkcorex.catalyst.ai.security;

import com.gkcorex.catalyst.ai.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthUtil {

  @Value("${jwt.secret-key}")
  private String jwtSecretKey;

  private SecretKey getSecretKey() {
    return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(User user) {
    return Jwts.builder()
        .subject(user.getUsername())
        .claim("userId", user.getId().toString())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 100))
        .signWith(getSecretKey())
        .compact();
  }

  public JwtUserPrincipal verifyAccessToken(String token) {
    Claims claims =
        Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token).getPayload();

    Long userId = Long.parseLong(claims.get("userId", String.class));
    String email = claims.getSubject();

    return new JwtUserPrincipal(userId, email, List.of());
  }

  public Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (Objects.isNull(authentication)
        || !((authentication.getPrincipal()) instanceof JwtUserPrincipal))
      throw new AuthenticationCredentialsNotFoundException("JWT not found");
    return ((JwtUserPrincipal) authentication.getPrincipal()).userId();
  }
}
