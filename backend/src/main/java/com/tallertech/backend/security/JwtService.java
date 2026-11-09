package com.tallertech.backend.security;

import com.tallertech.backend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expiration;
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expiration) {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        if (expiration <= 0) throw new IllegalArgumentException("JWT expiration must be positive");
        this.expiration = expiration;
    }
    public long getExpiration() { return expiration; }
    public String issue(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder().subject(user.getId()).issuer("tallertech")
                .claim("email", user.getEmail()).claim("role", user.getRole())
                .issuedAt(new Date(now)).expiration(new Date(now + expiration))
                .signWith(key, Jwts.SIG.HS256).compact();
    }
    public String userId(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer("tallertech").build()
                .parseSignedClaims(token).getPayload().getSubject();
    }
}
