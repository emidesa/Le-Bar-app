package com.barapp.backend.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

// Utilitaire pour générer et valider les tokens JWT
@Component
public class JwtUtils {

    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long EXPIRATION = 86400000; // 24h en millisecondes

    // Génère un token JWT avec l'email et le rôle de l'utilisateur
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    // Extrait l'email depuis le token
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Vérifie que le token est valide et non expiré
    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}