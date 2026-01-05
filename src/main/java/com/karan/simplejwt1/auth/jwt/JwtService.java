package com.karan.simplejwt1.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
@Slf4j
public class JwtService {

    private static final String SECRET = "357638792F423F4428472B4B6250655368566D597133743677397A2443264629";
    private static final long TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 days


    public String extractUserName(String token) {
        return getClaims(token , Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return getClaims(token , Claims::getExpiration);
    }

    public boolean validateToken(String token , String principalUsername) {
        final String username = extractUserName(token);
        return username.equals(principalUsername) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public <T> T getClaims(String token , Function<Claims, T> function){
        final Claims claims = getAllClaimsFromToken(token);
        return function.apply(claims);
    }

    public String generateToken(String username) {
        log.info("generateToken(-)");
        return Jwts.builder()
                .setSubject(username)
                .setIssuer("Authentication_Service")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
                .signWith(getSingKey() , SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
