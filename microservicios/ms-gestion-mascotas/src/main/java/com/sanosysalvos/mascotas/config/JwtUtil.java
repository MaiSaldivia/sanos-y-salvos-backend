package com.sanosysalvos.mascotas.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        String paddedSecret = (secret + "sanos-y-salvos-secret-2026-padding-key").substring(0, 32);
        this.key = Keys.hmacShaKeyFor(paddedSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extraerClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validarToken(String token) {
        Claims claims = extraerClaims(token);
        return claims != null && !claims.getExpiration().before(new Date());
    }

    public String extraerUserId(String token) {
        Claims claims = extraerClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public String extraerRol(String token) {
        Claims claims = extraerClaims(token);
        return claims != null ? claims.get("rol", String.class) : null;
    }
}
