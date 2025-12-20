package com.danil.library.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public String generateAccessToken(Long userId, String username, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlSeconds);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString()) // jti
                .setSubject(username)                // sub
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of(
                        "uid", userId,
                        "role", role,
                        "typ", "access"
                ))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public RefreshTokenData generateRefreshToken(Long userId, String username, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshTtlSeconds);
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .setId(jti)
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of(
                        "uid", userId,
                        "role", role,
                        "typ", "refresh"
                ))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new RefreshTokenData(token, jti, exp);
    }

    public Jws<Claims> parseAndValidate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public void assertTokenType(Claims claims, String expected) {
        String typ = claims.get("typ", String.class);
        if (!expected.equals(typ)) {
            throw new JwtException("Wrong token type: " + typ);
        }
    }

    public record RefreshTokenData(String token, String jti, Instant expiresAt) {}
}
