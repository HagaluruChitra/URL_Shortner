package com.chitra.urlshortener.auth;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private static final String DEFAULT_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private final javax.crypto.SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        String resolvedSecret = (secret == null || secret.isBlank()) ? DEFAULT_SECRET : secret;
        byte[] decodedKey;
        try {
            decodedKey = Decoders.BASE64.decode(resolvedSecret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("JWT_SECRET must be configured with a Base64-encoded key", ex);
        }
        if (decodedKey.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be configured with a Base64-encoded key at least 32 bytes long");
        }
        this.signingKey = Keys.hmacShaKeyFor(decodedKey);
        this.expirationMs = expirationMs;
    }

    public String issue(AuthenticatedUser user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getUsername()).claim("uid", user.id())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}