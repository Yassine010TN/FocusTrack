package com.focustrack.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "YourSuperSecretKeyForJwtYourSuperSecretKeyForJwtYourSuperSecretKeyForJwt".getBytes()); // ✅ 32+ characters

    @SuppressWarnings("deprecation")
    public String generateToken(Long userId, String email) { // ✅ Include user ID in the token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId); // ✅ Store userId in the JWT payload
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId)) // ✅ Set userId as the subject
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10-hour expiration
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public SecretKey getSecretKey() {
        return SECRET_KEY;
    }
}



