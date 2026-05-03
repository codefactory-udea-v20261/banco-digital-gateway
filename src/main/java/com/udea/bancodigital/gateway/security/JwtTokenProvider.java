package com.udea.bancodigital.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public Claims validateToken(String token) throws JwtException {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isTokenExpired(Claims claims) {
        try {
            return claims.getExpiration().before(new java.util.Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public String extractSubject(Claims claims) {
        return claims.getSubject();
    }

    public String extractRole(Claims claims) {
        // Map roles list to a single primary role string if that's what Gateway expects,
        // or just return the first one. Identity service puts them in "roles" (list).
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?> roles && !roles.isEmpty()) {
            return roles.get(0).toString();
        }
        return (String) claims.get("role");
    }
}
