package com.ibb.yurtlar.service;

import com.ibb.yurtlar.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    // JWT TOKEN ÜRETİYORUZ

    public String generateToken(
            AppUser user
    ) {
        Date issuedAt =
                new Date();

        Date expiration =
                new Date(
                        issuedAt.getTime() + expirationMs
                );

        JwtBuilder tokenBuilder =
                Jwts.builder()
                        .subject(user.getEmail())
                        .claim(
                                "userId",
                                user.getId()
                        )
                        .claim(
                                "role",
                                user.getRole().name()
                        )
                        .issuedAt(issuedAt)
                        .expiration(expiration);

        if (user.getAdminScope() != null) {
            tokenBuilder.claim(
                    "adminScope",
                    user.getAdminScope().name()
            );
        }

        if (user.getDormitory() != null) {
            tokenBuilder.claim(
                    "dormitoryId",
                    user.getDormitory().getId()
            );
        }

        return tokenBuilder
                .signWith(getSigningKey())
                .compact();
    }

    // TOKEN İÇİNDEN E-POSTAYI ÇIKARIR

    public String extractEmail(
            String token
    ) {
        return extractAllClaims(token)
                .getSubject();
    }

    //TOKEN KULLANICIYA AİT Mİ DEĞİL Mİ ? SÜRESİ DOLMUŞ MU ? KONTROL EDİLİR.
    public boolean isTokenValid(
            String token,
            AppUser user
    ) {
        String email =
                extractEmail(token);

        return email.equalsIgnoreCase(
                user.getEmail()
        ) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(
            String token
    ) {
        Date expiration =
                extractAllClaims(token)
                        .getExpiration();

        return expiration.before(
                new Date()
        );
    }

    private Claims extractAllClaims(
            String token
    ) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes =
                Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(
                keyBytes
        );
    }
}