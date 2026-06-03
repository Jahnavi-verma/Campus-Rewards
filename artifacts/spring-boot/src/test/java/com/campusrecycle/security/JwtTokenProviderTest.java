package com.campusrecycle.security;

import com.campusrecycle.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        AppProperties.Jwt jwt = props.getJwt();
        jwt.setSecret("test-secret-key-that-is-long-enough-32chars");
        jwt.setExpirationMs(86400000L);
        jwtTokenProvider = new JwtTokenProvider(props);
    }

    @Test
    void generateToken_isNotNull() {
        String token = jwtTokenProvider.generateToken("1", "test@test.com", "Test User");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generatedToken_isValid() {
        String token = jwtTokenProvider.generateToken("42", "student@uni.edu", "Student");
        assertTrue(jwtTokenProvider.isTokenValid(token));
    }

    @Test
    void getUserId_fromToken_isCorrect() {
        String token = jwtTokenProvider.generateToken("42", "student@uni.edu", "Student");
        assertEquals("42", jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    void invalidToken_returnsFalse() {
        assertFalse(jwtTokenProvider.isTokenValid("this.is.not.a.real.token"));
    }

    @Test
    void tamperedToken_returnsFalse() {
        String token = jwtTokenProvider.generateToken("1", "a@b.com", "User");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(jwtTokenProvider.isTokenValid(tampered));
    }

    @Test
    void emptyToken_returnsFalse() {
        assertFalse(jwtTokenProvider.isTokenValid(""));
    }
}
