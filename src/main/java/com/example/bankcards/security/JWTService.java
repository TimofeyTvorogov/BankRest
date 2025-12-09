package com.example.bankcards.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;


@Service
public class JWTService {

    @Value("${security.jwt.secret}")
    private String SECRET_BASE64_ENCODED;

    @Value("${security.jwt.lifetime:1800000}")
    private int tokenLifetimeMillis;



    public String generateToken(UserDetails userDetails) {
        MacAlgorithm alg = Jwts.SIG.HS256;
        return Jwts.builder()
                .claims()
                .issuer("auth_service")
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenLifetimeMillis))
                .and()
                .signWith(getKey(), alg)
                .compact();
    }
    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(getKey()).build()
                    .parseSignedClaims(token).getPayload();
    }


    public boolean isTokenValid(String token, UserDetails userDetails) throws JwtException {

        var sub = extractUsername(token);
        var exp = extractExpiration(token);
        return sub.equals(userDetails.getUsername()) && new Date().before(exp);



    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }
    public Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }



    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_BASE64_ENCODED));
    }
}
