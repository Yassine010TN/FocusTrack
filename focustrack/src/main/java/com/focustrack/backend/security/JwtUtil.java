package com.focustrack.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "YourSuperSecretKeyForJwtYourSuperSecretKeyForJwtYourSuperSecretKeyForJwt".getBytes()); // ✅ 32+ characters

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours expiration
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // ✅ Explicitly define algorithm
                .compact();
    }

    public SecretKey getSecretKey() {
        return SECRET_KEY;
    }
}


