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
    private final long expirationTime;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration:86400000}") long expiration) {
        String paddedSecret = (secret + "sanos-y-salvos-secret-2026-padding-key").substring(0, 32);
        this.key           = Keys.hmacShaKeyFor(paddedSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expiration;
    }

    public String generarToken(String idUsuario, String nombre, String email, Rol rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol",    rol.name());
        claims.put("nombre", nombre);
        claims.put("email",  email);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(idUsuario)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
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
        try {
            Claims claims = extraerClaims(token);
            return claims != null && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String extraerSubject(String token) {
        Claims claims = extraerClaims(token);
        return claims != null ? claims.getSubject() : null;
    }
}
