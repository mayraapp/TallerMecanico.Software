package com.taller.m01.security;

import com.taller.m01.config.AppSecurityProperties;
import com.taller.m01.entity.UserAccount;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.*;
import java.util.*;

@Service
public class JwtService {
    public record TokenData(Long userId, int sessionVersion, String jti, Instant expiresAt) { }
    private final AppSecurityProperties properties;
    private final SecretKey key;
    public JwtService(AppSecurityProperties properties) {
        this.properties = properties;
        try { this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getJwtSecret())); }
        catch (Exception ex) { throw new IllegalStateException("JWT_SECRET debe ser Base64 y tener al menos 32 bytes."); }
    }
    public String create(UserAccount user) {
        Instant now = Instant.now(); Instant expiration = now.plus(Duration.ofMinutes(properties.getJwtExpirationMinutes()));
        return Jwts.builder().subject(user.getId().toString()).id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now)).expiration(Date.from(expiration)).claim("sv", user.getSessionVersion()).signWith(key).compact();
    }
    public TokenData parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new TokenData(Long.valueOf(claims.getSubject()), claims.get("sv", Integer.class), claims.getId(), claims.getExpiration().toInstant());
    }
    public Instant expiration(String token) { return parse(token).expiresAt(); }
    public long expirationSeconds() { return properties.getJwtExpirationMinutes() * 60; }
}
