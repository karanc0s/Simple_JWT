package com.karan.simplejwt1.auth;


import com.karan.simplejwt1.auth.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    public void setup(){
        jwtService = new JwtService();
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken("user123");

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT structure
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtService.generateToken("user123");

        String username = jwtService.extractUserName(token);

        assertThat(username).isEqualTo("user123");
    }

    @Test
    void shouldExtractExpirationFromToken() {
        String token = jwtService.generateToken("user123");

        Date expiration = jwtService.extractExpiration(token);

        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        String token = jwtService.generateToken("user123");

        boolean valid = jwtService.validateToken(token, "user123");

        assertThat(valid).isTrue();
    }

    @Test
    void shouldFailValidationForWrongUsername() {
        String token = jwtService.generateToken("user123");

        boolean valid = jwtService.validateToken(token, "otherUser");

        assertThat(valid).isFalse();
    }







}
