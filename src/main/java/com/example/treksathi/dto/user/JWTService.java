package com.example.treksathi.dto.user;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService {

    public  String generateToken(int id, String email, String name, String role ){
        Map<String, Object> claims = new HashMap<>();

        claims.put("name", name);
        claims.put("id", id);
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt((new Date(System.currentTimeMillis())))
                .setExpiration((new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)))
                .signWith(getKey())
                .compact();
    }
    private Key getKey() {
//        byte[] keyBytes = Decoders.BASE64.decode();
//        return Keys.hmacShaKeyFor(keyBytes);
        return null;
    }
}
