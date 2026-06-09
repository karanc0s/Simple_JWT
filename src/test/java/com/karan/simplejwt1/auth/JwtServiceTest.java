package com.karan.simplejwt1.auth;


import com.karan.simplejwt1.auth.jwt.JwtService;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Generate a cryptographically secure 256-bit key encoded in Base64 for the HS256 algorithm
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();
        String base64Secret = Base64.getEncoder().encodeToString(keyBytes);

        // Inject the base64 secret into the private field using Spring's ReflectionTestUtils
        ReflectionTestUtils.setField(jwtService, "SECRET", base64Secret);
    }

    @Test
    void testGenerateToken_ShouldCreateValidAccessToken() {
        String username = "john_doe";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        String token = jwtService.generateToken(username, roles);

        assertNotNull(token);
        assertEquals(username, jwtService.extractUserName(token));
        assertEquals("ACCESS", jwtService.getTokenType(token));
        assertEquals(roles, jwtService.extractRoles(token));
    }

    @Test
    void testGenerateRefreshToken_ShouldCreateValidRefreshToken() {
        String username = "jane_doe";

        String token = jwtService.generateRefreshToken(username);

        assertNotNull(token);
        assertEquals(username, jwtService.extractUserName(token));
        assertEquals("REFRESH", jwtService.getTokenType(token));
        assertTrue(jwtService.extractRoles(token).isEmpty()); // Ensure no roles are attached
    }

    @Test
    void testValidateToken_ValidTokenAndUser_ShouldReturnTrue() {
        String username = "secure_user";
        String token = jwtService.generateToken(username, List.of("ROLE_USER"));

        boolean isValid = jwtService.validateToken(token, username);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidUsername_ShouldReturnFalse() {
        String username = "secure_user";
        String token = jwtService.generateToken(username, List.of("ROLE_USER"));

        boolean isValid = jwtService.validateToken(token, "wrong_user");

        assertFalse(isValid);
    }

    @Test
    void testExtractExpiration_ShouldBeInFuture() {
        String token = jwtService.generateToken("user", List.of());
        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
}
