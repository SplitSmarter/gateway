package com.onewordmax.splitsmart.gateway.utils.jwt;

import com.onewordmax.splitsmart.gateway.exceptions.TokenValidationException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final KeyLoader keyLoader;

    @Value("${token.validity.access}")
    private int accessTokenValiditySeconds;

    @Value("${token.validity.refresh}")
    private int refreshTokenValiditySeconds;

    /* ---------------- VALIDATION ---------------- */

    public Mono<Void> validateToken(String token) {
        return Mono.fromRunnable(() -> {
            try {
                parser().parseSignedClaims(token);
            } catch (JwtException e) {
                throw new TokenValidationException();
            }
        });
    }

    /* ---------------- EXTRACTION ---------------- */

    public Mono<String> extractId(String token) {
        return extractClaim(token, claims -> claims.get("id", String.class));
    }

    public Mono<String> extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public Mono<String> extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Add this to your JwtUtils.java
    public String extractSubjectSync(String token) {
        return parser()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private <T> Mono<T> extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {
        return Mono.fromSupplier(() -> {
            Claims claims = parser()
                    .parseSignedClaims(token)
                    .getPayload();
            return resolver.apply(claims);
        });
    }

    /* ---------------- TOKEN GENERATION ---------------- */

    public String generateAccessToken(
            String authId,
            String role,
            String refreshTokenSubject
    ) {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuer("onewordmax")
                .subject(refreshTokenSubject)
                .claim("id", authId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(
                        now.plus(accessTokenValiditySeconds, ChronoUnit.SECONDS)
                ))
                .signWith(keyLoader.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(String authId, String email) {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuer("onewordmax")
                .subject(UUID.randomUUID().toString())
                .claim("id", authId)
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(
                        now.plus(refreshTokenValiditySeconds, ChronoUnit.SECONDS)
                ))
                .signWith(keyLoader.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    /* ---------------- INTERNAL ---------------- */

    private JwtParser parser() {
        PublicKey publicKey = keyLoader.getPublicKey();
        return Jwts.parser()
                .verifyWith(publicKey)
                .build();
    }
}
