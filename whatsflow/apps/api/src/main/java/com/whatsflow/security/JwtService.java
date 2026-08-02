package com.whatsflow.security;


import com.whatsflow.security.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {
    private final JwtProperties props;

    public JwtService(JwtProperties props) { this.props = props; }

    private SecretKey key() {
        String secret = props.getSecret() == null ? "change-me-to-a-long-random-secret-key-32b" : props.getSecret();
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String createAccessToken(UUID userId, UUID tenantId, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.getAccessTokenTtl());
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(props.getIssuer())
                .subject(userId.toString())
                .claim("tenantId", tenantId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }
}
