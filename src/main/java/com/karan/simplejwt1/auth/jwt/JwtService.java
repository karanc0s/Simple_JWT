package com.karan.simplejwt1.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
public class JwtService {

    // Constants
    @Value("${app.jwt.jwt-secret}")
    private String SECRET;
    private static final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 15; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 days

    // ==========================================
    // EXTRACTION & VALIDATION
    // ==========================================

    public String extractUserName(String token) {
        return getClaims(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return getClaims(token, Claims::getExpiration);
    }

    public boolean validateToken(String token, String principalUsername) {
        final String username = extractUserName(token);
        return username.equals(principalUsername) && !isTokenExpired(token);
    }

    public List<String> extractRoles(String token) {
        return getClaims(token, claims -> {
            List<?> rawRoles = claims.get("roles", List.class);
            if (rawRoles == null) {
                return List.of(); // Return empty list instead of null
            }
            return rawRoles.stream()
                    .map(Object::toString)
                    .toList();
        });
    }

    public String getTokenType(String token) {
        return getClaims(token, claims -> claims.get("type", String.class));
    }

    // ==========================================
    // TOKEN GENERATION
    // ==========================================

    public String generateToken(String username, List<String> roles) {
        log.info("generateToken(-)");
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", roles);
        extraClaims.put("type", "ACCESS");

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuer("Authentication_Service")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        log.info("generateRefreshToken(-)");
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("type", "REFRESH");

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuer("Authentication_Service")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ==========================================
    // PRIVATE HELPERS
    // ==========================================

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private <T> T getClaims(String token, Function<Claims, T> function) {
        final Claims claims = getAllClaimsFromToken(token);
        return function.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
