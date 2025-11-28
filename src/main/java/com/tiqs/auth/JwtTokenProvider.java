package com.tiqs.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * @author TIQS
 * @date Created in 2025-11-28 14:48:12
 * @description JwtTokenProvider
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:eduSystemLoaderSecretKey2024ForJWTTokenGeneration}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(AuthContext authContext) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(authContext.userId().toString())
                .claim("role", authContext.role())
                .claim("username", authContext.username())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public AuthContext getAuthContextFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.valueOf(claims.getSubject());
        String role = claims.get("role", String.class);
        String username = claims.get("username", String.class);

        return new AuthContext(userId, username, UserRole.valueOf(role));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractTokenFromBearer(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}