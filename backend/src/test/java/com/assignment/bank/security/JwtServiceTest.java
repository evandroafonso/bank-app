package com.assignment.bank.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "minha-chave-super-secreta-com-pelo-menos-32-caracteres";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void shouldGenerateToken() {

        String token = jwtService.generateToken("john@email.com");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUsername() {

        String token = jwtService.generateToken("john@email.com");

        String username = jwtService.extractUsername(token);

        assertEquals("john@email.com", username);
    }

    @Test
    void shouldReturnTrueWhenTokenIsValid() {

        UserDetails userDetails =
                new User(
                        "john@email.com",
                        "password",
                        Collections.emptyList());

        String token = jwtService.generateToken("john@email.com");

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertTrue(valid);
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotMatch() {

        UserDetails userDetails =
                new User(
                        "mary@email.com",
                        "password",
                        Collections.emptyList());

        String token = jwtService.generateToken("john@email.com");

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertFalse(valid);
    }

    @Test
    void shouldReturnFalseWhenTokenIsExpired() {

        UserDetails userDetails =
                new User(
                        "john@email.com",
                        "password",
                        Collections.emptyList());

        String expiredToken = Jwts.builder()
                .subject("john@email.com")
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(
                        Keys.hmacShaKeyFor(SECRET.getBytes()),
                        Jwts.SIG.HS256)
                .compact();

        assertFalse(jwtService.isTokenValid(expiredToken, userDetails));
    }

    @Test
    void shouldThrowExceptionWhenTokenIsMalformed() {

        assertThrows(
                Exception.class,
                () -> jwtService.extractUsername("invalid-token"));
    }

    @Test
    void shouldThrowExceptionWhenTokenWasSignedWithAnotherKey() {

        String anotherSecret =
                "outra-chave-super-secreta-com-pelo-menos-32-caracteres";

        String token = Jwts.builder()
                .subject("john@email.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(
                        Keys.hmacShaKeyFor(anotherSecret.getBytes()),
                        Jwts.SIG.HS256)
                .compact();

        assertThrows(
                Exception.class,
                () -> jwtService.extractUsername(token));
    }
}