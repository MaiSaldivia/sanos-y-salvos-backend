package com.sanosysalvos.usuarios.config;

import com.sanosysalvos.usuarios.model.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final Key key;
    private final long EXPIRATION_TIME = 8 * 60 * 60 * 1000; // 8 hours in ms

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // Aseguramos que la clave tenga al menos 256 bits (32 bytes) para HS256
        String paddedSecret = (secret + "sanos-y-salvos-secret-2026-padding-key").substring(0, 32);
        this.key = Keys.hmacShaKeyFor(paddedSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(String idUsuario, String nombre, String email, Rol rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol.name());
        claims.put("nombre", nombre);
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(idUsuario)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
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

    public String extraerSubject(String token) {
        Claims claims = extraerClaims(token);
        return claims != null ? claims.getSubject() : null;
    }
}
